package com.azaala.vcs.async;

import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;

public class WorkerFactory {
    private static final WorkerFactory instance = new WorkerFactory();

    private WorkerFactory() {
    }

    public static WorkerFactory getInstance() {
        return instance;
    }

    public RepositoryInitWorker createRepositoryInitWorker(VCS vcs, String repositoryPath, ProgressListener progressListener) {
        return new RepositoryInitWorker(vcs, repositoryPath, progressListener);
    }

    public AddFileWorker createAddFileWorker(VCS vcs, Repository repository, String filePath, ProgressListener progressListener) {
        return new AddFileWorker(vcs, repository, filePath, progressListener);
    }

    public CommitWorker createCommitWorker(VCS vcs, Repository repository, String commitMessage, ProgressListener progressListener) {
        return new CommitWorker(vcs, repository, commitMessage, progressListener);
    }

    public DiffWorker createDiffWorker(VCS vcs, Repository repository, String commitId1, String commitId2, ProgressListener progressListener) {
        return new DiffWorker(vcs, repository, commitId1, commitId2, progressListener);
    }

    public HistoryLoadWorker createHistoryLoadWorker(VCS vcs, Repository repository, int limit, ProgressListener progressListener) {
        return new HistoryLoadWorker(vcs, repository, limit, progressListener);
    }

    public StatusLoadWorker createStatusLoadWorker(VCS vcs, Repository repository, ProgressListener progressListener) {
        return new StatusLoadWorker(vcs, repository, progressListener);
    }

    public AddFileWorker createAddFileWorker(VCS vcs, Repository repository, String filePath) {
        return new AddFileWorker(vcs, repository, filePath);
    }

    public CommitWorker createCommitWorker(VCS vcs, Repository repository, String commitMessage) {
        return new CommitWorker(vcs, repository, commitMessage);
    }

    public DiffWorker createDiffWorker(VCS vcs, Repository repository, String commitId1, String commitId2) {
        return new DiffWorker(vcs, repository, commitId1, commitId2);
    }

    public HistoryLoadWorker createHistoryLoadWorker(VCS vcs, Repository repository, int limit) {
        return new HistoryLoadWorker(vcs, repository, limit);
    }

    public StatusLoadWorker createStatusLoadWorker(VCS vcs, Repository repository) {
        return new StatusLoadWorker(vcs, repository);
    }
}

