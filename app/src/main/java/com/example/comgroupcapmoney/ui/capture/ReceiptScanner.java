package com.example.comgroupcapmoney.ui.capture;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Doc chu tren anh bang ML Kit (chay tren may) roi doan so tien (4A-3)
public final class ReceiptScanner {

    public interface Callback {
        // Goi tren main thread; khong doc duoc thi amount = ReceiptAmountParser.NOT_FOUND
        void onResult(long amount);
    }

    private ReceiptScanner() {}

    // imageFile: anh da nen va xoay dung chieu (ImageUtils.compressForUpload). Goi tu main thread.
    public static void scan(Context context, File imageFile, Callback cb) {
        InputImage image;
        try {
            image = InputImage.fromFilePath(context, Uri.fromFile(imageFile));
        } catch (Exception e) {
            cb.onResult(ReceiptAmountParser.NOT_FOUND);
            return;
        }
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(text -> cb.onResult(ReceiptAmountParser.parse(toRows(text))))
                .addOnFailureListener(e -> cb.onResult(ReceiptAmountParser.NOT_FOUND))
                .addOnCompleteListener(task -> recognizer.close());
    }

    // Hoa don thuong co nhan ben trai, so tien ben phai: ghep cac dong cung hang ngang thanh 1 dong
    private static List<String> toRows(Text text) {
        List<Text.Line> lines = new ArrayList<>();
        for (Text.TextBlock block : text.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                if (line.getBoundingBox() != null) {
                    lines.add(line);
                }
            }
        }
        Collections.sort(lines, (a, b) -> Integer.compare(a.getBoundingBox().centerY(), b.getBoundingBox().centerY()));

        List<String> rows = new ArrayList<>();
        List<Text.Line> row = new ArrayList<>();
        Rect rowBox = null;
        for (Text.Line line : lines) {
            Rect box = line.getBoundingBox();
            if (rowBox != null && Math.abs(box.centerY() - rowBox.centerY()) <= Math.max(box.height(), rowBox.height()) / 2) {
                row.add(line);
            } else {
                if (!row.isEmpty()) rows.add(joinRow(row));
                row = new ArrayList<>();
                row.add(line);
                rowBox = box;
            }
        }
        if (!row.isEmpty()) rows.add(joinRow(row));
        return rows;
    }

    private static String joinRow(List<Text.Line> row) {
        Collections.sort(row, (a, b) -> Integer.compare(a.getBoundingBox().left, b.getBoundingBox().left));
        StringBuilder sb = new StringBuilder();
        for (Text.Line l : row) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(l.getText());
        }
        return sb.toString();
    }
}
