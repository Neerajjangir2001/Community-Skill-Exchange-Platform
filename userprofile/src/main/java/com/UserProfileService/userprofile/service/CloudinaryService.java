package com.UserProfileService.userprofile.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.netty.util.internal.ObjectUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;


@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}")
            String cloudName,

            @Value("${cloudinary.api-key}")
            String apiKey,
            @Value("${cloudinary.api-secret}")
            String apiSecret
    ){
        this.cloudinary = new Cloudinary(Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));

    }

    public String uploadAvatar(UUID userId, MultipartFile file){
        try {
     Map uploadResult =  cloudinary.uploader().upload(
                    file.getBytes(), ObjectUtils.asMap(
                            "public_id", "avatars/" + userId,
                            "overwrite", true
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Avatar upload failed", e);
        }
    }


}
