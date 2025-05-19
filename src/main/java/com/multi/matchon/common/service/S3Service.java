package com.multi.matchon.common.service;

import com.multi.matchon.common.util.AwsS3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AwsS3Utils awsS3Utils;

    public void uploadProfileImage(Long memberId, MultipartFile file) {
        awsS3Utils.saveFile("attachments/profile/", memberId.toString(), file);
    }

    public String getProfileImageUrl(Long memberId) {
        return "https://sample-s3-multi18.s3.us-west-2.amazonaws.com/attachments/profile/" + memberId + ".jpg";
    }
}
