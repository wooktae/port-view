package my.portfolio.port_view.dto;

public class GetPriceRealTimeDTO {
    private String code;
    private String currentPrice;
    private String diff;
    private String volume;
    private String open;
    private String high;
    private String low;

    public GetPriceRealTimeDTO() {}

    // getters & setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(String currentPrice) { this.currentPrice = currentPrice; }
    public String getDiff() { return diff; }
    public void setDiff(String diff) { this.diff = diff; }
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    public String getOpen() { return open; }
    public void setOpen(String open) { this.open = open; }
    public String getHigh() { return high; }
    public void setHigh(String high) { this.high = high; }
    public String getLow() { return low; }
    public void setLow(String low) { this.low = low; }
}

