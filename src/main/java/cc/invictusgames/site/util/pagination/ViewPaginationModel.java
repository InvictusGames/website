package cc.invictusgames.site.util.pagination;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ViewPaginationModel<T> extends PaginationModel<T> {

    private int currentPage;
    private String key;

    public ViewPaginationModel(int currentPage, double modelsPerPage, List<T> models, String key) {
        super(modelsPerPage, models);
        this.currentPage = currentPage;
        this.key = key;
    }

    public int getNextPage() {
        return this.getNextPage(this.currentPage);
    }

    public int getLastPage() {
        return this.getLastPage(this.currentPage);
    }

}
