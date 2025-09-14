import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

/**
 * Utility class for determining the version of the project based on Git tags.
 */
class Versioning {

    /**
     * Get the version of the project based on Git tags.
     * - If HEAD has a tag → return that tag.
     * - Otherwise → return latest tag by commit history + abbreviated commit hash.
     * - If no tags exist → return "0.0.0-<abbreviatedHash>".
     *
     * @param projectRootDir the root directory of the project (containing the .git directory)
     * @return the version string
     */
    static String getVersion(File projectRootDir) {
        try (
                Repository repository = new FileRepositoryBuilder()
                        .setGitDir(new File(projectRootDir, ".git"))
                        .readEnvironment()
                        .findGitDir()
                        .build()
                Git git = new Git(repository)
        ) {
            def headId = repository.resolve("HEAD")
            if (headId == null) {
                System.err.println("HEAD not found in repository")
                return "0.0.0"
            }

            RevCommit headCommit
            try (RevWalk revWalk = new RevWalk(repository)) {
                headCommit = revWalk.parseCommit(headId)
            }

            def tags = git.tagList().call()
            if (tags.isEmpty()) {
                def hash = headCommit.id.name().substring(0, 7)
                return "0.0.0-${hash}"
            }

            // Check if HEAD is exactly on a tag
            Ref tagOnHead = tags.find { ref ->
                def commitId = ref.getPeeledObjectId() ?: ref.getObjectId()
                commitId == headCommit.id
            }
            if (tagOnHead != null) {
                return tagOnHead.getName().replaceAll('refs/tags/', '')
            }

            // Map each tag to the commit it points to
            Map<Ref, RevCommit> tagToCommit = [:]
            try (RevWalk revWalk = new RevWalk(repository)) {
                tags.each { Ref ref ->
                    def commitId = ref.getPeeledObjectId() ?: ref.getObjectId()
                    RevCommit commit = revWalk.parseCommit(commitId)
                    tagToCommit[ref] = commit
                }

                // Walk history from HEAD backwards
                revWalk.markStart(headCommit)
                for (RevCommit c : revWalk) {
                    def tagRef = tagToCommit.find { it.value == c }?.key
                    if (tagRef != null) {
                        def tagName = tagRef.getName().replaceAll('refs/tags/', '')
                        def abbrevHash = headCommit.getId().name().substring(0, 7)
                        return "${tagName}-${abbrevHash}"
                    }
                }
            }

            // Fallback if no tag found in history
            def fallbackHash = headCommit.getId().name().substring(0, 7)
            return "0.0.0-${fallbackHash}"

        } catch (Exception e) {
            System.err.println("Error determining version: " + e)
            return "0.0.0"
        }
    }
}
