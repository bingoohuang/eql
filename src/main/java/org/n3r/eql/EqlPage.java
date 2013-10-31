package org.n3r.eql;


public class EqlPage {
    private int startIndex;
    private int pageRows;
    private int totalRows;

    public EqlPage() {

    }

    public EqlPage(int startIndex, int pageRows) {
        this.setStartIndex(startIndex);
        this.setPageRows(pageRows);
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getPageRows() {
        return pageRows;
    }

    public void setPageRows(int pageRows) {
        this.pageRows = pageRows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getCurrentPage() {
        return getPageRows() <= 0 ? 1 : (getStartIndex() + getPageRows()) / getPageRows();
    }

    public void setCurrentPage(int currentPage) {
        this.startIndex = getPageRows() <= 0 ? 0 : (currentPage - 1) * getPageRows();
    }

    public int getTotalPages() {
        int totalPages = getPageRows() <= 0 ? 1 : (getTotalRows() + getPageRows() - 1) / getPageRows();
        return totalPages < 1 ? 1 : totalPages;
    }


    @Override
    public String toString() {
        return "EqlPage{" +
                "startIndex=" + startIndex +
                ", pageRows=" + pageRows +
                ", totalRows=" + totalRows +
                '}';
    }
}
