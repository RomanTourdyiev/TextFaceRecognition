// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.arondevice.facespotter.ui.camera;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.arondevice.facespotter.TextActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;

/**
 * Processor for the text recognition demo.
 */
public class TextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

    private static final String TAG = "TextRecProc";

    private final FirebaseVisionTextRecognizer detector;
    private final TextActivity activityInstance;
    private boolean enableOverlay = false;

    public TextRecognitionProcessor(TextActivity activity, boolean enableOverlay) {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        activityInstance = activity;
        this.enableOverlay = enableOverlay;
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionText results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (enableOverlay) {
            if (originalCameraImage != null) {
                CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay,
                        originalCameraImage);
                graphicOverlay.add(imageGraphic);
            }
            List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                    for (int k = 0; k < elements.size(); k++) {
                        GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay,
                                elements.get(k));
                        graphicOverlay.add(textGraphic);
                    }
                }
            }
            graphicOverlay.postInvalidate();
        }

        activityInstance.returnResults(results);
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }
}
