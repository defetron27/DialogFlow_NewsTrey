package Models;

import java.util.List;

public class NewsResponseModel
{
    private String status;
    private Integer totalResults;
    private List<NewsArticlesModel> articles;

    public NewsResponseModel() {
    }

    public NewsResponseModel(String status, Integer totalResults, List<NewsArticlesModel> articles) {
        this.status = status;
        this.totalResults = totalResults;
        this.articles = articles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public List<NewsArticlesModel> getArticles() {
        return articles;
    }

    public void setArticles(List<NewsArticlesModel> articles) {
        this.articles = articles;
    }
}
