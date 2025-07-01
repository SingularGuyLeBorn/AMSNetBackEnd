package com.scy.mytemplate.service.impl;

import com.scy.mytemplate.service.FileService;
import com.scy.mytemplate.model.entity.FileEntity;
import com.scy.mytemplate.mapper.FileEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文件实体服务实现类
 */
@Service
public class FileServiceImpl implements FileService {

    private final FileEntityMapper fileEntityMapper;

    @Autowired
    public FileServiceImpl(FileEntityMapper fileEntityMapper) {
        this.fileEntityMapper = fileEntityMapper;
    }

    @Override
    public int insertFileEntity(FileEntity fileEntity) {
        return fileEntityMapper.insertFileEntity(fileEntity);
    }
}