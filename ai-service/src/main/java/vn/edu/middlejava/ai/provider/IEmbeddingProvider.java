package vn.edu.middlejava.ai.provider;

import java.util.List;

public interface IEmbeddingProvider {
    List<Double> embed(String text);
}
