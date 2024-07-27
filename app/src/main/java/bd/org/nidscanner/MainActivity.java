package bd.org.nidscanner;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import bd.org.nidscanner.databinding.ActivityMainBinding;
import bd.org.nidscanner.parser.DataParser;
import bd.org.nidscanner.parser.NewNidDataParser;
import bd.org.nidscanner.parser.OldNidDataParser;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.scanBtn.setOnClickListener(v -> {
            startScan();
        });
    }



    private void startScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.PDF_417);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(false);
        options.setOrientationLocked(true);
        options.setPrompt("Place the barcode inside the rectangle to scan");
        barcodeLauncher.launch(options);
    }

    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result == null) {
                    Toast.makeText(this, getString(R.string.scan_failed_please_try_again), Toast.LENGTH_LONG).show();
                    promptRetry();
                } else {
                    if (result.getContents() == null) {
                        Toast.makeText(this, getString(R.string.scan_failed_please_try_again), Toast.LENGTH_LONG).show();
                        promptRetry();
                    } else {
                        String rawData = result.getContents();
                        Utils.CARD_TYPE cardType = Utils.getCardType(rawData);
                        if (cardType == Utils.CARD_TYPE.SMART_NID_CARD) {
                            NewNidDataParser parser = new NewNidDataParser(this, rawData);
                            saveCardData(parser);
                        } else if (cardType == Utils.CARD_TYPE.OLD_NID_CARD) {
                            OldNidDataParser parser = new OldNidDataParser(this, rawData);
                            saveCardData(parser);
                        } else {
                            Toast.makeText(this, getString(R.string.invalid_nid_card), Toast.LENGTH_LONG).show();
                            promptRetry();
                        }
                    }
                }
            });
    private void saveCardData(DataParser parser) {
        String name = parser.getName();
        String nidNo = parser.getNidNo();
        String dateOfBirth = parser.getDateOfBirth();
        String issueDate = parser.getIssueDate();
        String rawData = parser.getRawData();
    }

    private void promptRetry() {
        Toast.makeText(this, getString(R.string.would_you_like_to_retry_scanning), Toast.LENGTH_LONG).show();
        binding.scanBtn.setText(R.string.retry_scan);
        binding.scanBtn.setOnClickListener(v -> startScan());
    }
}