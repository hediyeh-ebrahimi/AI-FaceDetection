package com.image.imageProccessing.controller;

import com.image.imageProccessing.model.Image;
import com.image.imageProccessing.service.ImageService;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.feature.FacePatchFeature;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.similarity.FaceSimilarityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class MainController {

    private ImageService imageService;

    @Autowired
    public MainController(ImageService imageService) {
        this.imageService = imageService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {

        File folder = new File("src/main/resources/static/images/");
        List<Image> images = new ArrayList<>();
        File[] listOfFiles = folder.listFiles();
//        for (File file0 : listOfFiles) {
//            if (file0.isFile()) {
//                Image image = new Image();
//                image.setName(file0.getName());
//                //image.setAddress("src/main/resources/static/images/"+file0.getName());
//                image.setAddress("/images/" + file0.getName());
//                images.add(image);
//            }
//        }


        model.addAttribute("images", images);

        return "index";
    }

    @RequestMapping(value = "/uploadArchiveImages", method = RequestMethod.POST)
    public String uploadArchiveImages(@RequestParam("file") MultipartFile file, Model model) {
        String uploadDir = "src/main/resources/static/images/";
        if (file.getSize() == 0) {
            model.addAttribute("error", "تصویری انتخاب نشده است");
        } else {


            try {
                String name = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
                try {
                    Path copyLocation = Paths
                            .get(uploadDir + File.separator + StringUtils.cleanPath(name));
                    Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
                    Image image = new Image();
                    image.setOriginalName(file.getOriginalFilename());
                    image.setHashedName(name);
                    image.setAddress(uploadDir + name);
                    this.imageService.addNewItem(image);
                    model.addAttribute("success", "آپلود با موفقیت انجام شد");

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Could not store file " + file.getOriginalFilename()
                            + ". Please try again!");
                    model.addAttribute("error", "آپلود با خطا مواجه شد");
                }


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not store file " + file.getOriginalFilename()
                        + ". Please try again!");
                model.addAttribute("error", "آپلود با خطا مواجه شد");
            }
        }
        return "index";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile file, Model model) {
        String uploadDir = "src/main/resources/static/imagesUploaded";
        List<Image> images = new ArrayList<>();
        if (file.getSize() == 0) {
            model.addAttribute("error_chosen", "تصویری انتخاب نشده است");
        } else {
            String name = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            try {
                Path copyLocation = Paths
                        .get(uploadDir + File.separator + StringUtils.cleanPath(name));
                Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

                // show list images
                Map<String, Image> map = new ConcurrentHashMap();
                final FImage query = ImageUtilities.readF(
                        new File("src/main/resources/static/imagesUploaded/" + name));

            /*File folder = new File("src/main/resources/static/images/");
            File[] listOfFiles = folder.listFiles();
            for (File file0 : listOfFiles) {
                if (file0.isFile()) {*/

                List<Image> imagesArchive = this.imageService.findAll();
                for (Image file0 : imagesArchive) {
                    if (file0.getAddress() != null) {

//                Image image = new Image();
//                image.setName(file0.getName());
//                image.setAddress("/images/"+file0.getName());
//                images.add(image);
                        final FImage target = ImageUtilities.readF(new File(file0.getAddress()));

                        final HaarCascadeDetector detector = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
                        final FKEFaceDetector kedetector = new FKEFaceDetector(detector);
                        final FacePatchFeature.Extractor extractor = new FacePatchFeature.Extractor();
                        final FaceFVComparator<FacePatchFeature, FloatFV> comparator =
                                new FaceFVComparator<>(FloatFVComparison.EUCLIDEAN);
                        final FaceSimilarityEngine<KEDetectedFace, FacePatchFeature, FImage> engine =
                                new FaceSimilarityEngine<>(kedetector, extractor, comparator);
                        engine.setQuery(target, "target");
                        engine.setTest(query, "query");
                        engine.performTest();
                        for (final Map.Entry<String, Map<String, Double>> e : engine.getSimilarityDictionary().entrySet()) {
                            double bestScore = Double.MAX_VALUE;
                            String best = null;
                            for (final Map.Entry<String, Double> matches : e.getValue().entrySet()) {
                                if (matches.getValue() < bestScore) {
                                    bestScore = matches.getValue();
                                    best = matches.getKey();
                                }
                            }
                            if (bestScore > 65.0) {
                                if (!map.containsKey(file0.getHashedName())) {
                                    Image image = new Image();
                                    image.setHashedName(file0.getHashedName());
                                    image.setOriginalName(file0.getOriginalName());
                                    image.setAddress("/images/" + file0.getHashedName());
                                    images.add(image);
                                    map.put(file0.getHashedName(), image);
                                    System.out.println("found!");
                                }
                            }
//                    final FImage img = new FImage(target.width + query.width, Math.max(target.height, query.height));
//                    img.drawImage(target, 0, 0);
//                    img.drawImage(query, target.width, 0);
//                    img.drawShape(engine.getBoundingBoxes().get(e.getKey()), 1F);
//                    final Rectangle r = engine.getBoundingBoxes().get(best);
//                    r.translate(target.width, 0);
//                    img.drawShape(r, 1F);
                            //ImageUtilities.write(img, new File("src/main/resources/static/images/output1.jpg"));
                        }


                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not store file " + file.getOriginalFilename()
                        + ". Please try again!");
            }


            File fileToDelete = new File("src/main/resources/static/imagesUploaded/" + name);
            boolean success = fileToDelete.delete();
        }


        model.addAttribute("images", images);

        return "index";
    }
}
