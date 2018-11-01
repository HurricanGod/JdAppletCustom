package cn.hurrican.app.service;

import cn.hurrican.mapper.UnionUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnionUserService {

    @Autowired
    private UnionUserMapper mapper;
}
