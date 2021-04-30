package com.image.imageProccessing.repository;

import com.image.imageProccessing.model.Image;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository  extends CrudRepository<Image,Long> {
}
