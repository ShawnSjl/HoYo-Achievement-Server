package tech.sjiale.hoyo_achievement_server.util;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

@Slf4j
public class GitUtils {

    /**
     * Clone or pull git remote repo.
     *
     * @param repoUrl  remote repo url; repo should be public
     * @param destPath local directory of git repo
     */
    public static void cloneOrPull(String repoUrl, String destPath) {
        File localDir = new File(destPath);

        try {
            if (isGitRepo(localDir)) {
                log.info("Git repo already exists, pull latest changes.");
                doPull(localDir);
            } else {
                log.info("Git repo doesn't exist, clone from remote.");
                doClone(repoUrl, localDir);
            }
        } catch (Exception e) {
            log.error("Clone or pull git repo failed.", e);
        }
    }

    /**
     * Pull latest changes from remote repo
     *
     * @param localDir local directory of git repo
     * @throws IOException     error caused by IO
     * @throws GitAPIException error caused by git API
     */
    private static void doPull(File localDir) throws IOException, GitAPIException {
        try (Git git = Git.open(localDir)) {
            git.pull()
                    .setCredentialsProvider(null)
                    .call();
            log.info("Git repo pulled successfully.");
        }
    }

    /**
     * Clone repo from remote
     *
     * @param repoUrl  remote repo url
     * @param localDir local directory of git repo
     * @throws GitAPIException error caused by git API
     */
    private static void doClone(String repoUrl, File localDir) throws GitAPIException {
        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localDir)
                .setCredentialsProvider(null) // Use null credentials, since repo is public
                .call()
                .close();
        log.info("Git repo cloned successfully.");
    }

    /**
     * Check if the given directory is a git repo
     *
     * @param dir directory
     * @return true if git repo, false otherwise
     */
    private static boolean isGitRepo(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return false;
        File gitDir = new File(dir, ".git");
        return gitDir.exists() && gitDir.isDirectory();
    }
}
