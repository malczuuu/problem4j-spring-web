import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class Versioning {
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
            if (tags.isEmpty()) {
                def hash = headCommit.id.name().substring(0, 7)
                return "0.0.0-${hash}"
            }

            def tagOnHead = tags.find {
                Ref ref ->
                    def commitId = ref.getPeeledObjectId() ?: ref.getObjectId()
                    commitId == headCommit.id
            }
            if (tagOnHead != null) {
                return tagOnHead.getName().replaceAll('refs/tags/', '')
            }

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
