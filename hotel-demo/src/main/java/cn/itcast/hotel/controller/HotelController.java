package cn.itcast.hotel.controller;

import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestPrams;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hotel")
@ResponseBody
public class HotelController {

    @Autowired
    private IHotelService hotelService;

    @PostMapping("/list")
    public PageResult page(@RequestBody RequestPrams requestPrams) {
        return hotelService.selectPage(requestPrams);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> filter(@RequestBody RequestPrams requestPrams) {
        return hotelService.filters(requestPrams);
    }

    @GetMapping("/suggestion")
    public List<String> suggestion(@RequestParam String key) {
        return hotelService.suggestion(key);
    }
}
