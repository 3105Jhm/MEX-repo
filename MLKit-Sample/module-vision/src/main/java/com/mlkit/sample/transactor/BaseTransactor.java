/**
 *  Copyright 2018 Google LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  2020.2.21-Changed name from VisionProcessorBase to BaseTransactor.
 *  2020.2.21-Deleted method: process(Bitmap bitmap, GraphicOverlay graphicOverlay,
 *             String path,boolean flag);
 *      process(Bitmap bitmap, GraphicOverlay graphicOverlay,String path);
 *      onSuccess(
 *             @Nullable Bitmap originalCameraImage,
 *             @NonNull T results,
 *             @NonNull FrameMetadata frameMetadata,
 *             @NonNull GraphicOverlay graphicOverlay, String path, boolean flag);
 *      onSuccess(
 *             @Nullable Bitmap originalCameraImage,
 *             @NonNull T results,
 *             @NonNull FrameMetadata frameMetadata,
 *             @NonNull GraphicOverlay graphicOverlay, String path);
 *      writeFileSdcard(String message);
 *                   Huawei Technologies Co., Ltd.
 */

package com.mlkit.sample.transactor;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.mlkit.sample.camera.CameraConfiguration;
import com.mlkit.sample.util.BitmapUtils;
import com.mlkit.sample.camera.FrameMetadata;
import com.mlkit.sample.util.CommonUtils;
import com.mlkit.sample.views.overlay.GraphicOverlay;
import com.huawei.hms.mlsdk.common.MLFrame;

import java.nio.ByteBuffer;

public abstract class BaseTransactor<T> implements ImageTransactor {

    // To keep the latest images and its metadata.
    private ByteBuffer latestImage;

    private FrameMetadata latestImageMetaData;

    // To keep the images and metadata in process.
    private ByteBuffer transactingImage;

    private FrameMetadata transactingMetaData;

    public BaseTransactor() {
    }

    @Override
    public synchronized void process(ByteBuffer data, final FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) {
        this.latestImage = data;
        this.latestImageMetaData = frameMetadata;
        if (this.transactingImage == null && this.transactingMetaData == null) {
            this.processLatestImage(graphicOverlay);
        }
    }

    @Override
    public void process(Bitmap bitmap, GraphicOverlay graphicOverlay) {
        MLFrame frame = new MLFrame.Creator().setBitmap(bitmap).create();
        this.detectInVisionImage(bitmap, frame, null, graphicOverlay);
    }

    private synchronized void processLatestImage(GraphicOverlay graphicOverlay) {
        this.transactingImage = this.latestImage;
        this.transactingMetaData = this.latestImageMetaData;
        this.latestImage = null;
        this.latestImageMetaData = null;
        if (this.transactingImage != null && this.transactingMetaData != null) {
            int width;
            int height;
            if (this.isFaceDetection()) {
                width = CameraConfiguration.DEFAULT_WIDTH;
                height = CameraConfiguration.DEFAULT_HEIGHT;
            } else {
                width = this.transactingMetaData.getWidth();
                height = this.transactingMetaData.getHeight();
            }
            MLFrame.Property metadata =
                    new MLFrame.Property.Creator()
                            .setFormatType(ImageFormat.NV21)
                            .setWidth(width)
                            .setHeight(height)
                            .setQuadrant(this.transactingMetaData.getRotation())
                            .create();
            Bitmap bitmap = BitmapUtils.getBitmap(this.transactingImage, this.transactingMetaData);
            if (this.isFaceDetection()) {
                ByteBuffer resizeImage = ByteBuffer.allocate((CameraConfiguration.DEFAULT_WIDTH * CameraConfiguration.DEFAULT_HEIGHT * 3) / 2 + 1);
                CommonUtils.handleByteBuffer(this.transactingImage, resizeImage,
                        transactingMetaData.getWidth(), transactingMetaData.getHeight(),
                        CameraConfiguration.DEFAULT_WIDTH, CameraConfiguration.DEFAULT_HEIGHT);
                this.detectInVisionImage(
                        bitmap, MLFrame.fromByteBuffer(resizeImage, metadata), this.transactingMetaData, graphicOverlay);
            } else {
                this.detectInVisionImage(
                        bitmap, MLFrame.fromByteBuffer(this.transactingImage, metadata), this.transactingMetaData, graphicOverlay);
            }

        }
    }

    private void detectInVisionImage(final Bitmap bitmap, MLFrame image, final FrameMetadata metadata,
                                     final GraphicOverlay graphicOverlay) {
        this.detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<T>() {
                            @Override
                            public void onSuccess(T results) {
                                if (metadata == null || metadata.getCameraFacing() == CameraConfiguration.getCameraFacing()) {
                                    BaseTransactor.this.onSuccess(bitmap, results, metadata, graphicOverlay);
                                }
                                BaseTransactor.this.processLatestImage(graphicOverlay);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                BaseTransactor.this.onFailure(e);
                            }
                        });
    }

    @Override
    public void stop() {
    }

    protected abstract Task<T> detectInImage(MLFrame image);

    /**
     * Callback that executes with a successful detection result.
     * @param originalCameraImage hold the original image from camera, used to draw the background
     *                            image.
     * @param results
     * @param frameMetadata
     * @param graphicOverlay
     */

    protected abstract void onSuccess(
            Bitmap originalCameraImage,
            T results,
            FrameMetadata frameMetadata, GraphicOverlay graphicOverlay);

    protected abstract void onFailure(Exception e);

    @Override
    public boolean isFaceDetection() {
        return false;
    }
}
