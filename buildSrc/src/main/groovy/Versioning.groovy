import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

/**
 * Utility class for determining the version of the project based on Git tags.
 */
class Versioning {

    /**
     * Get the version of the project based on Git tags. If the current commit has a tag, return that tag.  If not,
     * return the latest tag with the abbreviated commit hash. If no tags are found, return "0.0.0-<abbreviatedHash>".
     *
     * @param projectRootDir the root directory of the project (containing the .git directory)
     * @return the version string
     */
    static String getVersion(File projectRootDir) {
        try {
            def repository = new FileRepositoryBuilder()
                    .setGitDir(new File(projectRootDir, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build()

            def git = new Git(repository)

            def headCommit = new RevWalk(repository).parseCommit(repository.resolve("HEAD"))
            def tags = git.tagList().call()

            // ----------
            // if no tags found, return 0.0.0-<abbreviatedHash>
            if (tags.isEmpty()) {
                def hash = headCommit.id.name().substring(0, 7)
                return "0.0.0-${hash}"
            }

            // ----------
            // check if there's a tag on the current HEAD and return it if found
            def tagOnHead = tags.find {
                Ref ref ->
                    def commitId = ref.getPeeledObjectId() ?: ref.getObjectId()
                    commitId == headCommit.id
            }
            if (tagOnHead != null) {
                return tagOnHead.getName().replaceAll('refs/tags/', '')
            }

            // ----------
            // find the latest tag by commit time and return <latestTag>-<abbreviatedHash>
            def latestTag = tags.collect {
                Ref ref ->
                    def commitId = ref.getPeeledObjectId() ?: ref.getObjectId()
                    def commit = new RevWalk(repository).parseCommit(commitId)
                    [ref: ref, commit: commit] as Map<String, RevCommit>
            }.max {
                it.commit.getCommitTime()
            }

            def tagName = latestTag.ref.getName().replaceAll('refs/tags/', '')
            def abbrevHash = headCommit.getId().name().substring(0, 7)

            return "${tagName}-${abbrevHash}"
        } catch (Exception ignored) {
            return "0.0.0"
        }
    }
}
