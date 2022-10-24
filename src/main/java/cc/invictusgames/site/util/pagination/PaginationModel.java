package cc.invictusgames.site.util.pagination;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter @Getter
public class PaginationModel<T> {

    private final double modelsPerPage;
    private final List<T> models;

    public PaginationModel(double modelsPerPage, List<T> models) {
        this.modelsPerPage = modelsPerPage;
        this.models = models;
    }

    public int getPages() {
        return (int) Math.ceil(this.models.size() / this.modelsPerPage);
    }

    public int getNextPage(int currentPage) {
        return Math.min(currentPage + 1, this.getPages());
    }

    public int getLastPage(int currentPage) {
        if (currentPage - 1 <= 0)
            return 1;

        return currentPage - 1;
    }

    public List<T> getPage(int page) {
        int pagesRequired = this.getPages();
        if (page > pagesRequired)
            page = pagesRequired;

        if (page <= 0)
            page = 1;

        List<T> toReturn = new ArrayList<>();
        int startingAmount = (int) (this.modelsPerPage * page - this.modelsPerPage);

        for (int i = startingAmount; i < this.modelsPerPage * page; i++) {
            if (i >= 0 && i < this.models.size())
                toReturn.add(this.models.get(i));
        }

        return toReturn;
    }

    public void addModels(List<T> ts) {
        this.models.addAll(ts);
    }

    public void addModel(T t) {
        this.models.add(t);
    }
}
