package cn.itcast.hotel.pojo;

import lombok.Data;

@Data
public class RequestPrams {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String brand;
    private String starName;
    private String city;
    private Integer minPrice;
    private Integer maxPrice;
    private String location;
}
