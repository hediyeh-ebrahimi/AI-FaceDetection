package com.image.imageProccessing.service;

import com.image.imageProccessing.model.Image;
import com.image.imageProccessing.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ImageService {

    private ImageRepository imageRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public void addNewItem(Image image) {
        this.imageRepository.save(image);
    }

    public List<Image> findAll() {
        return (List<Image>) this.imageRepository.findAll();
    }
}
