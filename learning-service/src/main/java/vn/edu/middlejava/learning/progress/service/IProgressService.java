package vn.edu.middlejava.learning.progress.service;

import vn.edu.middlejava.learning.progress.dto.ProgressResponse;

public interface IProgressService {
    ProgressResponse getForUser(Long userId);
}
