/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.mlkit.sample.callback;

import android.graphics.Bitmap;

import com.mlkit.sample.views.overlay.GraphicOverlay;
import com.huawei.hms.mlsdk.document.MLDocument;
import com.huawei.hms.mlsdk.text.MLText;

public interface CouldInfoResultCallBack {
    void onSuccessForText(Bitmap originalCameraImage, MLText text, GraphicOverlay graphicOverlay);

    void onSuccessForDoc(Bitmap originalCameraImage, MLDocument text, GraphicOverlay graphicOverlay);
}
