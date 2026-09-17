package com.one.utility.core.router

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val title: String) {
    IMAGES("Images"),
    PDF("PDF"),
    SCANNER("Scanner"),
    FILES("Storage & Files"),
    CALCULATOR("Calculators"),
    CONVERTER("Converters"),
    MONEY("Money"),
    DATE_TIME("Date & Time"),
    TEXT("Text Tools"),
    TECH("Privacy & Tech")
}

class ToolDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    route: String,
    val keywords: List<String>,
    val badge: String? = null
) {
    val route: String = when {
        route.startsWith("tool/") -> route
        id == "image_to_pdf" -> "image_to_pdf"
        id == "background_remover" -> "background_remover"
        id == "cool_fonts" -> "cool_fonts"
        id == "calc_standard" || id == "calc_scientific" -> "calculator"
        id == "image_compressor" -> "compressor"
        id == "image_converter" -> "image_converter"
        id == "image_cropper" -> "image_cropper"
        id == "scanner_doc" -> "document_scanner"
        id == "storage_analyzer" -> "storage_cleaner"
        id == "batch_file_renamer" -> "batch_rename"
        id == "qr_generator_tool" -> "qr"
        else -> "tool/$id"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ToolDefinition) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

object ToolRegistry {
    val tools = listOf(
        // ==========================================
        // 📸 Category 1: IMAGES (25 Tools)
        // ==========================================
        ToolDefinition(
            id = "image_to_pdf",
            title = "Image → PDF",
            description = "Combine photos into clean A4 or Letter PDF with custom margins",
            category = ToolCategory.IMAGES,
            route = "image_to_pdf",
            keywords = listOf("image", "photo", "picture", "pdf", "photos to pdf", "turn these photos into a pdf", "make a pdf"),
            badge = "POPULAR"
        ),
        ToolDefinition(
            id = "multiple_images_to_pdf",
            title = "Multiple Images → One PDF",
            description = "Batch import multiple photos into a single multi-page PDF document",
            category = ToolCategory.IMAGES,
            route = "image_to_pdf",
            keywords = listOf("multiple images", "batch pdf", "combine photos", "multi photo pdf")
        ),
        ToolDefinition(
            id = "pdf_to_images",
            title = "PDF → Images",
            description = "Render and export PDF pages as high-resolution PNG or JPG images",
            category = ToolCategory.IMAGES,
            route = "pdf_toolbox",
            keywords = listOf("pdf to images", "pdf to jpg", "pdf to png", "extract pdf pages to image"),
            badge = "HIGH-RES"
        ),
        ToolDefinition(
            id = "image_compressor",
            title = "Image Compressor",
            description = "Reduce file size up to 85% without noticeable quality loss",
            category = ToolCategory.IMAGES,
            route = "compressor",
            keywords = listOf("compress", "smaller", "shrink", "reduce size", "make this image smaller", "compression"),
            badge = "BATCH"
        ),
        ToolDefinition(
            id = "image_resizer",
            title = "Image Resizer",
            description = "Resize dimensions to 1080px, 1920px or custom aspect ratios",
            category = ToolCategory.IMAGES,
            route = "resizer",
            keywords = listOf("resize", "dimensions", "1080px", "1920px", "aspect ratio", "scale")
        ),
        ToolDefinition(
            id = "image_converter",
            title = "Image Converter",
            description = "Convert between PNG, JPG, and WEBP formats with quality control",
            category = ToolCategory.IMAGES,
            route = "image_converter",
            keywords = listOf("convert image", "png to jpg", "jpg to png", "convert webp", "image format"),
            badge = "OFFLINE"
        ),
        ToolDefinition(
            id = "image_cropper",
            title = "Image Cropper",
            description = "Free crop, 1:1, 4:3, 16:9 aspect ratios, 90° rotation and flip",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("crop", "crop photo", "crop image", "aspect ratio")
        ),
        ToolDefinition(
            id = "image_rotator",
            title = "Image Rotator",
            description = "Rotate photos 90°, 180°, or 270° clockwise with loss-free orientation",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("rotate", "rotate image", "turn image", "90 degrees")
        ),
        ToolDefinition(
            id = "image_flipper",
            title = "Image Flipper",
            description = "Flip images horizontally (mirror) or vertically",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("flip", "flip image", "mirror image", "horizontal flip", "vertical flip")
        ),
        ToolDefinition(
            id = "background_remover",
            title = "Background Remover",
            description = "On-device AI cutout with Photoshop-level smooth edge feathering",
            category = ToolCategory.IMAGES,
            route = "background_remover",
            keywords = listOf("remove background", "cutout", "transparent bg", "no background", "remove bg", "background removal"),
            badge = "AI LOCAL"
        ),
        ToolDefinition(
            id = "background_changer",
            title = "Background Changer",
            description = "Replace photo backgrounds with solid studio slate, blue, or custom colors",
            category = ToolCategory.IMAGES,
            route = "background_remover",
            keywords = listOf("change background", "background color", "studio background", "replace bg")
        ),
        ToolDefinition(
            id = "image_blur",
            title = "Image Blur",
            description = "Apply Gaussian blur to sensitive details or create soft depth effects",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("blur", "blur image", "gaussian blur", "soft focus")
        ),
        ToolDefinition(
            id = "image_pixelator",
            title = "Image Pixelator",
            description = "Pixelate faces, license plates, or create retro mosaic effects",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("pixelate", "mosaic", "censor", "hide face")
        ),
        ToolDefinition(
            id = "image_sharpen",
            title = "Image Sharpen",
            description = "Enhance fine details, textures, and edge clarity in photos",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("sharpen", "clarity", "enhance details")
        ),
        ToolDefinition(
            id = "image_grayscale",
            title = "Image Grayscale",
            description = "Convert color photos into classic monochrome black and white",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("grayscale", "black and white", "monochrome", "b&w photo")
        ),
        ToolDefinition(
            id = "image_brightness",
            title = "Image Brightness",
            description = "Adjust photo exposure, highlight luminance, and shadow levels",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("brightness", "exposure", "lighting", "brighten photo")
        ),
        ToolDefinition(
            id = "image_contrast",
            title = "Image Contrast",
            description = "Tweak dynamic range between dark shadows and bright highlights",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("contrast", "dynamic range", "high contrast")
        ),
        ToolDefinition(
            id = "image_saturation",
            title = "Image Saturation",
            description = "Boost vibrant colors or desaturate toward muted pastel tones",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("saturation", "vibrant", "color boost")
        ),
        ToolDefinition(
            id = "image_metadata_viewer",
            title = "Image Metadata Viewer",
            description = "Inspect EXIF headers, camera model, ISO, shutter speed, and capture date",
            category = ToolCategory.IMAGES,
            route = "storage_cleaner",
            keywords = listOf("exif", "metadata", "image details", "camera info", "iso")
        ),
        ToolDefinition(
            id = "remove_image_metadata",
            title = "Remove Image Metadata",
            description = "Strip sensitive EXIF data, GPS coordinates, and camera info for privacy",
            category = ToolCategory.IMAGES,
            route = "image_converter",
            keywords = listOf("strip exif", "remove metadata", "remove gps", "clean photo", "privacy")
        ),
        ToolDefinition(
            id = "screenshot_cropper",
            title = "Screenshot Cropper",
            description = "Rapid crop tool tailored to trim status bars and navigation notches",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("screenshot", "crop screenshot", "status bar crop")
        ),
        ToolDefinition(
            id = "batch_image_compressor",
            title = "Batch Image Compressor",
            description = "Compress dozens of photos simultaneously with space savings summary",
            category = ToolCategory.IMAGES,
            route = "compressor",
            keywords = listOf("batch compress", "bulk compress", "compress photos", "shrink album"),
            badge = "BATCH"
        ),
        ToolDefinition(
            id = "batch_image_resizer",
            title = "Batch Image Resizer",
            description = "Uniformly scale multiple images to a fixed width or height in one pass",
            category = ToolCategory.IMAGES,
            route = "resizer",
            keywords = listOf("batch resize", "bulk resize", "resize multiple")
        ),
        ToolDefinition(
            id = "batch_image_converter",
            title = "Batch Image Converter",
            description = "Convert entire folders of images to WEBP, PNG, or JPG simultaneously",
            category = ToolCategory.IMAGES,
            route = "image_converter",
            keywords = listOf("batch convert", "bulk convert", "convert all photos")
        ),
        ToolDefinition(
            id = "batch_image_renamer",
            title = "Batch Image Renamer",
            description = "Rename photo collections with sequential numbering and dates",
            category = ToolCategory.IMAGES,
            route = "batch_rename",
            keywords = listOf("batch rename images", "rename photos", "photo naming")
        ),

        // ==========================================
        // 📄 Category 2: PDF (21 Tools)
        // ==========================================
        ToolDefinition(
            id = "pdf_image_to_pdf",
            title = "Image → PDF",
            description = "Convert pictures into standardized, printable PDF documents",
            category = ToolCategory.PDF,
            route = "image_to_pdf",
            keywords = listOf("image to pdf", "photos to pdf", "pdf create")
        ),
        ToolDefinition(
            id = "pdf_merge",
            title = "Merge PDFs",
            description = "Combine multiple PDF files into one ordered master document",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("merge pdf", "combine pdf", "join pdf", "merge documents"),
            badge = "POPULAR"
        ),
        ToolDefinition(
            id = "pdf_split",
            title = "Split PDF",
            description = "Separate large PDF files by page ranges or extract individual sheets",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("split pdf", "divide pdf", "extract pages", "split documents")
        ),
        ToolDefinition(
            id = "pdf_to_images_page",
            title = "PDF → Images",
            description = "Render and export every PDF page as high-resolution PNG or JPG files",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("pdf to images", "pdf pages to image", "pdf to png")
        ),
        ToolDefinition(
            id = "pdf_compressor",
            title = "PDF Compressor",
            description = "Shrink PDF file sizes for quick email attachment and messaging",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("compress pdf", "shrink pdf", "reduce pdf size", "small pdf")
        ),
        ToolDefinition(
            id = "pdf_rotate",
            title = "Rotate PDF",
            description = "Fix upside-down or sideways pages by 90° clockwise increments",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("rotate pdf", "turn pdf", "orient pdf")
        ),
        ToolDefinition(
            id = "pdf_reorder",
            title = "Reorder PDF Pages",
            description = "Re-arrange and change the sequence of pages in your document",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("reorder pdf", "move pages", "rearrange pdf")
        ),
        ToolDefinition(
            id = "pdf_delete_pages",
            title = "Delete PDF Pages",
            description = "Remove unwanted blank or duplicate pages from any PDF",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("delete pages", "remove pdf pages", "clean pdf")
        ),
        ToolDefinition(
            id = "pdf_extract_pages",
            title = "Extract PDF Pages",
            description = "Save specific page selections (e.g. 1-3, 5) as a new standalone PDF",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("extract pages", "page selection", "save pages")
        ),
        ToolDefinition(
            id = "pdf_duplicate_pages",
            title = "Duplicate PDF Pages",
            description = "Clone selected pages within a document for form duplications",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("duplicate pages", "copy pdf page", "clone page")
        ),
        ToolDefinition(
            id = "pdf_page_counter",
            title = "PDF Page Counter",
            description = "Inspect total page count and document dimensions instantly",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("page count", "count pdf pages", "pdf length")
        ),
        ToolDefinition(
            id = "pdf_add_text",
            title = "Add Text to PDF",
            description = "Insert custom notes, captions, and text fields onto document pages",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("add text to pdf", "annotate pdf", "type on pdf")
        ),
        ToolDefinition(
            id = "pdf_add_signature",
            title = "Add Signature to PDF",
            description = "Place and stamp your saved signature on document contracts and forms",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("sign pdf", "add signature", "fill and sign", "stamp signature")
        ),
        ToolDefinition(
            id = "pdf_watermark",
            title = "Add Watermark",
            description = "Stamp confidential or draft watermarks diagonally across pages",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("watermark", "stamp pdf", "confidential watermark")
        ),
        ToolDefinition(
            id = "pdf_metadata_viewer",
            title = "PDF Metadata Viewer",
            description = "Inspect author, title, creation date, and PDF producer info",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("pdf metadata", "pdf info", "pdf author")
        ),
        ToolDefinition(
            id = "pdf_metadata_editor",
            title = "PDF Metadata Editor",
            description = "Modify title, subject, and author metadata tags in PDF files",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("edit metadata", "pdf author edit", "change pdf title")
        ),
        ToolDefinition(
            id = "pdf_password_protect",
            title = "PDF Password Protection",
            description = "Encrypt PDF documents with offline AES password security",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("lock pdf", "password protect pdf", "encrypt pdf")
        ),
        ToolDefinition(
            id = "pdf_unlock",
            title = "PDF Unlock / Decrypt",
            description = "Remove passwords from authorized protected PDF documents",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("unlock pdf", "remove pdf password", "decrypt pdf")
        ),
        ToolDefinition(
            id = "pdf_page_size_converter",
            title = "PDF Page Size Converter",
            description = "Resize PDF sheets between A4, US Letter, Legal, and A3 dimensions",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("pdf page size", "a4 to letter", "resize pdf pages")
        ),
        ToolDefinition(
            id = "pdf_scanner",
            title = "PDF Scanner",
            description = "Digitize multi-page paper documents directly into a PDF",
            category = ToolCategory.PDF,
            route = "document_scanner",
            keywords = listOf("pdf scanner", "scan to pdf", "scan multi page")
        ),
        ToolDefinition(
            id = "batch_pdf_processing",
            title = "Batch PDF Processing",
            description = "Apply batch compression or extraction across multiple PDFs",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("batch pdf", "bulk pdf", "process multiple pdfs")
        ),

        // ==========================================
        // 📷 Category 3: SCANNER (12 Tools)
        // ==========================================
        ToolDefinition(
            id = "scanner_doc",
            title = "Document Scanner",
            description = "Scan paper notes and documents with crisp B&W and color enhancement",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("scan document", "document scanner", "scan paper", "doc scan"),
            badge = "PRO SCAN"
        ),
        ToolDefinition(
            id = "scanner_id",
            title = "ID / Document Scanner",
            description = "Scan identity cards, licenses, and passports with high-contrast edge crop",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("id scanner", "scan id", "passport scan", "card scanner")
        ),
        ToolDefinition(
            id = "scanner_receipt",
            title = "Receipt Scanner",
            description = "Digitize shopping receipts with enhanced contrast for expense filing",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("receipt scanner", "scan receipt", "expense receipt")
        ),
        ToolDefinition(
            id = "scanner_receipt_card",
            title = "Business Card Scanner",
            description = "High-res business card digitization with clean cropping",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("business card", "scan business card", "vcard scan")
        ),
        ToolDefinition(
            id = "scanner_multi_page",
            title = "Multi-page Scanner",
            description = "Continuous camera capture mode to scan multi-page booklets",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("multi page scan", "book scan", "batch scan")
        ),
        ToolDefinition(
            id = "scanner_auto_edge",
            title = "Auto Edge Detection",
            description = "Intelligent boundary locator to automatically detect document corners",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("edge detection", "auto crop", "document borders")
        ),
        ToolDefinition(
            id = "scanner_perspective",
            title = "Perspective Correction",
            description = "Quad-corner deskew to straighten angled camera captures",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("perspective correction", "straighten scan", "deskew")
        ),
        ToolDefinition(
            id = "scan_to_pdf",
            title = "Scan → PDF",
            description = "1-tap pipeline to scan notes directly into a multi-page PDF",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("scan to pdf", "paper to pdf", "scanner pdf"),
            badge = "PIPELINE"
        ),
        ToolDefinition(
            id = "scan_to_image",
            title = "Scan → Image",
            description = "Capture clean scans directly saved as high-res PNG or JPG files",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("scan to image", "scan to jpg", "scan to png")
        ),
        ToolDefinition(
            id = "scanner_ocr",
            title = "OCR",
            description = "On-device Optical Character Recognition to detect text in images",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("ocr", "read text from image", "text recognition")
        ),
        ToolDefinition(
            id = "ocr_to_text",
            title = "OCR → Text",
            description = "Extract readable text from photos and copy directly to clipboard",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("ocr to text", "extract text from image", "copy text from photo")
        ),
        ToolDefinition(
            id = "signature_capture",
            title = "Signature Capture",
            description = "Draw, capture, and export transparent signature PNG files",
            category = ToolCategory.SCANNER,
            route = "document_scanner",
            keywords = listOf("signature", "capture signature", "draw signature", "sign")
        ),

        // ==========================================
        // 🧹 Category 4: STORAGE & FILES (17 Tools)
        // ==========================================
        ToolDefinition(
            id = "storage_analyzer",
            title = "Storage Analyzer",
            description = "Analyze internal storage breakdown (Total, Free, Used space)",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("storage analyzer", "disk space", "storage info", "memory usage"),
            badge = "AUDIT"
        ),
        ToolDefinition(
            id = "large_file_finder",
            title = "Large File Finder",
            description = "Locate space-hogging videos, archives, and files over 10MB",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("large files", "big files", "find heavy files")
        ),
        ToolDefinition(
            id = "duplicate_file_finder",
            title = "Duplicate File Finder",
            description = "Detect exact duplicate files using cryptographic SHA-256 hashing",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("duplicate files", "find duplicates", "duplicate photos"),
            badge = "DE-DUPE"
        ),
        ToolDefinition(
            id = "similar_image_finder",
            title = "Similar Image Finder",
            description = "Detect near-identical photos and burst shots to recover space",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("similar images", "burst photos", "duplicate photos")
        ),
        ToolDefinition(
            id = "screenshot_finder",
            title = "Screenshot Finder",
            description = "Group old screenshots and screen captures for bulk cleanup",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("screenshots", "find screenshots", "clean screenshots")
        ),
        ToolDefinition(
            id = "download_finder",
            title = "Download Finder",
            description = "Inspect forgotten downloads and old installers in local storage",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("downloads", "find downloads", "old downloads")
        ),
        ToolDefinition(
            id = "old_file_finder",
            title = "Old File Finder",
            description = "Identify untouched and stale files that haven't been accessed recently",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("old files", "stale files", "unused files")
        ),
        ToolDefinition(
            id = "file_size_analyzer",
            title = "File Size Analyzer",
            description = "Detailed byte measurement and format distribution breakdown",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("file size", "size breakdown", "folder weight")
        ),
        ToolDefinition(
            id = "file_information",
            title = "File Information",
            description = "Inspect MIME types, exact byte count, URI paths, and checksums",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("file info", "mime type", "file details")
        ),
        ToolDefinition(
            id = "file_renamer",
            title = "File Renamer",
            description = "Rename individual files with instant validation and preview",
            category = ToolCategory.FILES,
            route = "batch_rename",
            keywords = listOf("rename file", "single rename", "change filename")
        ),
        ToolDefinition(
            id = "batch_file_renamer",
            title = "Batch File Renamer",
            description = "Rename files in bulk using pattern search and sequential numbering",
            category = ToolCategory.FILES,
            route = "batch_rename",
            keywords = listOf("batch rename", "rename files", "renamer", "bulk rename"),
            badge = "BATCH"
        ),
        ToolDefinition(
            id = "file_extension_changer",
            title = "File Extension Changer",
            description = "Batch update file extensions safely (e.g. .jpeg to .jpg)",
            category = ToolCategory.FILES,
            route = "batch_rename",
            keywords = listOf("extension changer", "change extension", "file format rename")
        ),
        ToolDefinition(
            id = "zip_creator",
            title = "ZIP Creator",
            description = "Compress multiple files into a clean standard ZIP archive offline",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("zip creator", "make zip", "compress zip", "archive")
        ),
        ToolDefinition(
            id = "zip_extractor",
            title = "ZIP Extractor",
            description = "Unpack ZIP archives directly into your device storage",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("unzip", "extract zip", "zip extractor", "open zip")
        ),
        ToolDefinition(
            id = "folder_size_analyzer",
            title = "Folder Size Analyzer",
            description = "Recursive directory size analyzer to trace heavy directories",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("folder size", "directory size", "large folders")
        ),
        ToolDefinition(
            id = "temporary_file_cleaner",
            title = "Temporary File Cleaner",
            description = "Safely clear cached app data and leftover temporary files",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("clean cache", "clear cache", "temp files", "cleaner"),
            badge = "SAFE"
        ),
        ToolDefinition(
            id = "empty_folder_finder",
            title = "Empty Folder Finder",
            description = "Scan and clean empty orphan directories to declutter storage",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("empty folders", "clean empty directories", "empty dirs")
        ),

        // ==========================================
        // 🧮 Category 5: CALCULATORS (27 Tools)
        // ==========================================
        ToolDefinition(
            id = "calc_standard",
            title = "Standard Calculator",
            description = "Large tactile keypad arithmetic (+, -, ×, ÷) with haptic feedback",
            category = ToolCategory.CALCULATOR,
            route = "calculator",
            keywords = listOf("calc", "standard calculator", "basic math", "arithmetic")
        ),
        ToolDefinition(
            id = "calc_scientific",
            title = "Scientific Calculator",
            description = "Trigonometry (sin, cos, tan), log, ln, square roots, and powers",
            category = ToolCategory.CALCULATOR,
            route = "calculator",
            keywords = listOf("scientific calculator", "trig", "sin", "cos", "log", "powers"),
            badge = "POPULAR"
        ),
        ToolDefinition(
            id = "calc_percentage",
            title = "Percentage Calculator",
            description = "Instant percentage finder (e.g. What's 15% of ₦850,000?)",
            category = ToolCategory.CALCULATOR,
            route = "calculator",
            keywords = listOf("percentage", "percent", "% of", "calculate percent")
        ),
        ToolDefinition(
            id = "calc_discount",
            title = "Discount Calculator",
            description = "Calculate sales markdowns and saved money instantly",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("discount", "sale", "save", "markdown")
        ),
        ToolDefinition(
            id = "calc_tax",
            title = "Tax Calculator",
            description = "Compute sales tax, VAT, and total checkout amounts",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("tax", "vat", "sales tax", "gst")
        ),
        ToolDefinition(
            id = "calc_tip",
            title = "Tip Calculator",
            description = "Calculate service gratuity with fair percentage presets",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("tip", "gratuity", "restaurant tip")
        ),
        ToolDefinition(
            id = "calc_split_bill",
            title = "Split Bill Calculator",
            description = "Divide restaurant bills and tips evenly among group members",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("split bill", "split", "group bill", "divide bill")
        ),
        ToolDefinition(
            id = "calc_profit",
            title = "Profit Calculator",
            description = "Calculate net profit and return on investment from sales",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("profit", "net profit", "earnings")
        ),
        ToolDefinition(
            id = "calc_profit_margin",
            title = "Profit Margin Calculator",
            description = "Analyze profit margins vs markup percentage on goods",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("margin", "profit margin", "gross margin")
        ),
        ToolDefinition(
            id = "calc_markup",
            title = "Markup Calculator",
            description = "Determine retail selling prices based on wholesale cost and markup",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("markup", "wholesale markup", "selling price")
        ),
        ToolDefinition(
            id = "calc_simple_interest",
            title = "Simple Interest Calculator",
            description = "Calculate simple interest yield over time (I = P·R·T)",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("simple interest", "interest yield", "p r t")
        ),
        ToolDefinition(
            id = "calc_compound_interest",
            title = "Compound Interest Calculator",
            description = "Multi-year compound interest growth with custom compounding",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("compound interest", "investment growth", "annual compounding")
        ),
        ToolDefinition(
            id = "calc_loan",
            title = "Loan Calculator",
            description = "Monthly payment, interest amortization, and total repayment breakdown",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("loan", "loan payment", "amortization")
        ),
        ToolDefinition(
            id = "calc_mortgage",
            title = "Mortgage Calculator",
            description = "Home loan payment estimator with principal and interest curves",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("mortgage", "home loan", "property payment")
        ),
        ToolDefinition(
            id = "calc_emi",
            title = "EMI Calculator",
            description = "Equated Monthly Installment calculator for car and personal loans",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("emi", "equated monthly installment", "car loan emi")
        ),
        ToolDefinition(
            id = "calc_savings",
            title = "Savings Calculator",
            description = "Projected wealth savings accumulation with recurring deposits",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("savings", "savings goal", "future wealth")
        ),
        ToolDefinition(
            id = "calc_investment_return",
            title = "Investment Return Calculator",
            description = "Calculate ROI and annualized compound returns on investments",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("roi", "investment return", "return on investment")
        ),
        ToolDefinition(
            id = "calc_ratio",
            title = "Ratio Calculator",
            description = "Solve proportions, aspect ratios, and scaling factors (A:B = C:D)",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("ratio", "proportion", "aspect ratio calculator", "scale")
        ),
        ToolDefinition(
            id = "calc_fraction",
            title = "Fraction Calculator",
            description = "Add, subtract, multiply, and reduce mixed fractions",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("fraction", "fractions", "mixed fractions", "reduce fraction")
        ),
        ToolDefinition(
            id = "calc_average",
            title = "Average Calculator",
            description = "Compute arithmetic mean, median, mode, and range for number sets",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("average", "mean", "median", "mode")
        ),
        ToolDefinition(
            id = "calc_age",
            title = "Age Calculator",
            description = "Exact age in years, months, days, and total days lived",
            category = ToolCategory.CALCULATOR,
            route = "currency_time",
            keywords = listOf("age", "how old am i", "age calculator", "birthday")
        ),
        ToolDefinition(
            id = "calc_date_diff",
            title = "Date Difference Calculator",
            description = "Measure elapsed calendar days between two dates",
            category = ToolCategory.CALCULATOR,
            route = "currency_time",
            keywords = listOf("date difference", "days between", "date duration")
        ),
        ToolDefinition(
            id = "calc_time_diff",
            title = "Time Difference Calculator",
            description = "Calculate exact hours and minutes elapsed between two times",
            category = ToolCategory.CALCULATOR,
            route = "currency_time",
            keywords = listOf("time difference", "hours between", "elapsed time")
        ),
        ToolDefinition(
            id = "calc_fuel_cost",
            title = "Fuel Cost Calculator",
            description = "Trip fuel cost and required fuel volume based on mileage",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("fuel cost", "gas trip", "fuel consumption", "petrol cost")
        ),
        ToolDefinition(
            id = "calc_speed",
            title = "Speed Calculator",
            description = "Velocity calculator based on travel distance and elapsed time",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("speed", "velocity", "speed calculator")
        ),
        ToolDefinition(
            id = "calc_distance",
            title = "Distance Calculator",
            description = "Compute travel distance given vehicle speed and trip duration",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("distance", "distance calculator", "trip distance")
        ),
        ToolDefinition(
            id = "calc_bmi",
            title = "BMI Calculator",
            description = "Body Mass Index calculator with WHO weight classification categories",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("bmi", "body mass index", "weight category")
        ),

        // ==========================================
        // 🔄 Category 6: CONVERTERS (17 Tools)
        // ==========================================
        ToolDefinition(
            id = "conv_length",
            title = "Length",
            description = "Meters, feet, inches, yards, centimeters, and millimeters",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("length", "feet to meters", "inches to cm", "yards")
        ),
        ToolDefinition(
            id = "conv_distance",
            title = "Distance",
            description = "Kilometers, miles, nautical miles, astronomical units, light years",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("distance", "miles to km", "light year", "parsec", "astronomy")
        ),
        ToolDefinition(
            id = "conv_area",
            title = "Area",
            description = "Square meters, square feet, acres, hectares, square kilometers",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("area", "acres", "hectares", "sq ft to sq m")
        ),
        ToolDefinition(
            id = "conv_volume",
            title = "Volume",
            description = "Liters, milliliters, gallons, cubic meters, fluid ounces, cups",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("volume", "liters to gallons", "ml to oz", "cubic meters")
        ),
        ToolDefinition(
            id = "conv_weight",
            title = "Weight",
            description = "Grams, kilograms, pounds, ounces, stones, metric tons",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("weight", "kg to lbs", "pounds to kg", "ounces")
        ),
        ToolDefinition(
            id = "conv_mass",
            title = "Mass",
            description = "Milligrams, grams, kilograms, atomic mass units, carats",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("mass", "carats", "milligrams", "grams")
        ),
        ToolDefinition(
            id = "conv_temp",
            title = "Temperature",
            description = "Celsius, Fahrenheit, Kelvin, and Rankine temperature scales",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("temperature", "celsius to fahrenheit", "kelvin", "f to c")
        ),
        ToolDefinition(
            id = "conv_speed",
            title = "Speed",
            description = "km/h, mph, knots, m/s, Mach, and speed of light",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("speed", "kmh to mph", "knots", "mach")
        ),
        ToolDefinition(
            id = "conv_time",
            title = "Time",
            description = "Seconds, minutes, hours, days, weeks, months, years",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("time", "hours to minutes", "seconds to hours", "days")
        ),
        ToolDefinition(
            id = "conv_data_storage",
            title = "Data Storage",
            description = "Bytes, KB, MB, GB, TB, PB (1024 binary vs 1000 decimal)",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("data storage", "gb to mb", "mb to kb", "bytes")
        ),
        ToolDefinition(
            id = "conv_energy",
            title = "Energy",
            description = "Joules, kilojoules, calories, kilocalories (food Cal), BTU, kWh",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("energy", "joules to calories", "kwh", "btu")
        ),
        ToolDefinition(
            id = "conv_power",
            title = "Power",
            description = "Watts, kilowatts, horsepower, metric horsepower, foot-pounds/sec",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("power", "watts to horsepower", "kilowatts", "hp")
        ),
        ToolDefinition(
            id = "conv_pressure",
            title = "Pressure",
            description = "Pascals, bars, PSI, atmospheres (atm), Torr, mmHg",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("pressure", "psi to bar", "atmospheres", "pascals")
        ),
        ToolDefinition(
            id = "conv_frequency",
            title = "Frequency",
            description = "Hertz, kilohertz, megahertz, gigahertz, RPM",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("frequency", "hz to mhz", "ghz", "rpm")
        ),
        ToolDefinition(
            id = "conv_angle",
            title = "Angle",
            description = "Degrees, radians, gradians, arcminutes, arcseconds",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("angle", "degrees to radians", "radians to degrees")
        ),
        ToolDefinition(
            id = "conv_fuel_economy",
            title = "Fuel Economy",
            description = "MPG (US), MPG (UK), L/100km, km/L",
            category = ToolCategory.CONVERTER,
            route = "unit_converter",
            keywords = listOf("fuel economy", "mpg to l 100km", "gas mileage")
        ),
        ToolDefinition(
            id = "conv_currency",
            title = "Currency",
            description = "Offline currency converter with customizable exchange rate presets",
            category = ToolCategory.CONVERTER,
            route = "currency_time",
            keywords = listOf("currency", "exchange rate", "money converter", "rates")
        ),

        // ==========================================
        // 💰 Category 7: MONEY TOOLS (12 Tools)
        // ==========================================
        ToolDefinition(
            id = "money_currency",
            title = "Currency Converter",
            description = "Convert currencies using custom rates without internet dependence",
            category = ToolCategory.MONEY,
            route = "currency_time",
            keywords = listOf("currency converter", "usd to eur", "naira", "exchange"),
            badge = "OFFLINE"
        ),
        ToolDefinition(
            id = "money_discount",
            title = "Discount Calculator",
            description = "Determine discounted pricing and exact savings at checkout",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("discount calculator", "markdown", "money saved")
        ),
        ToolDefinition(
            id = "money_tax",
            title = "Tax Calculator",
            description = "Compute sales tax, goods and services tax, and VAT percentages",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("tax calculator", "vat calculator", "sales tax")
        ),
        ToolDefinition(
            id = "money_tip",
            title = "Tip Calculator",
            description = "Quick service gratuity calculation with percentage presets",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("tip calculator", "gratuity calculator")
        ),
        ToolDefinition(
            id = "money_split_bill",
            title = "Split Bill",
            description = "Divide group dinner bills and contributions per person",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("split bill", "bill split", "dinner split")
        ),
        ToolDefinition(
            id = "money_profit_loss",
            title = "Profit / Loss",
            description = "Track net earnings or losses on business inventory",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("profit loss", "p&l", "net profit")
        ),
        ToolDefinition(
            id = "money_markup",
            title = "Markup",
            description = "Calculate retail markup percentage over wholesale manufacturing costs",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("markup calculator", "wholesale markup")
        ),
        ToolDefinition(
            id = "money_margin",
            title = "Margin",
            description = "Analyze gross and net profit margin percentages",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("margin calculator", "profit margin")
        ),
        ToolDefinition(
            id = "money_loan_payment",
            title = "Loan Payment",
            description = "Monthly loan installment breakdown with total interest paid",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("loan payment", "monthly installment", "interest paid")
        ),
        ToolDefinition(
            id = "money_interest",
            title = "Interest",
            description = "Simple and compound interest accumulation over time",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("interest calculator", "accrued interest")
        ),
        ToolDefinition(
            id = "money_savings_goal",
            title = "Savings Goal",
            description = "Determine how much to deposit monthly to achieve your target",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("savings goal", "target savings", "monthly deposit")
        ),
        ToolDefinition(
            id = "money_compound_growth",
            title = "Compound Growth",
            description = "Long-term investment compound interest forecasting",
            category = ToolCategory.MONEY,
            route = "everyday_calculators",
            keywords = listOf("compound growth", "wealth growth", "compound investment")
        ),

        // ==========================================
        // 📅 Category 8: DATE & TIME (12 Tools)
        // ==========================================
        ToolDefinition(
            id = "time_world_clock",
            title = "World Clock",
            description = "Monitor local times across major international cities",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("world clock", "international time", "clock", "time in tokyo")
        ),
        ToolDefinition(
            id = "time_zone_converter",
            title = "Time Zone Converter",
            description = "Convert meeting times between timezones using local tzdb",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("time zone", "convert timezone", "gmt to est", "lagos to tokyo"),
            badge = "TZDB"
        ),
        ToolDefinition(
            id = "time_date_calculator",
            title = "Date Calculator",
            description = "Add or subtract days, weeks, or months to any calendar date",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("date calculator", "add days", "subtract days")
        ),
        ToolDefinition(
            id = "time_date_difference",
            title = "Date Difference",
            description = "Measure elapsed years, months, and days between dates",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("date difference", "days between dates", "how many days")
        ),
        ToolDefinition(
            id = "time_age_calculator",
            title = "Age Calculator",
            description = "Exact age in years, months, days, and next birthday countdown",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("age calculator", "how old", "birthday countdown")
        ),
        ToolDefinition(
            id = "time_countdown",
            title = "Countdown",
            description = "Precision countdown timer for upcoming milestones and events",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("countdown", "event countdown", "milestone timer")
        ),
        ToolDefinition(
            id = "time_stopwatch",
            title = "Stopwatch",
            description = "High-precision stopwatch with lap splits and millisecond accuracy",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("stopwatch", "lap timer", "precision timer")
        ),
        ToolDefinition(
            id = "time_timer",
            title = "Timer",
            description = "Multipurpose interval timer with tactile completion vibration",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("timer", "countdown timer", "interval")
        ),
        ToolDefinition(
            id = "time_alarm_shortcuts",
            title = "Alarm / Reminder shortcuts",
            description = "Quick access triggers for system alarms and reminders",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("alarm", "reminder", "set alarm")
        ),
        ToolDefinition(
            id = "time_unix_timestamp",
            title = "Unix Timestamp Converter",
            description = "Convert epoch seconds to human-readable datetime",
            category = ToolCategory.DATE_TIME,
            route = "dev_tools",
            keywords = listOf("unix timestamp", "epoch time", "convert timestamp")
        ),
        ToolDefinition(
            id = "time_week_number",
            title = "Week Number Calculator",
            description = "Determine ISO-8601 calendar week numbers for dates",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("week number", "calendar week", "iso week")
        ),
        ToolDefinition(
            id = "time_business_days",
            title = "Business Days Calculator",
            description = "Calculate working days between dates excluding weekends",
            category = ToolCategory.DATE_TIME,
            route = "currency_time",
            keywords = listOf("business days", "working days", "workdays between")
        ),

        // ==========================================
        // ✍️ Category 9: TEXT TOOLS (17 Tools)
        // ==========================================
        ToolDefinition(
            id = "text_word_counter",
            title = "Word Counter",
            description = "Real-time word count analysis for essays and speeches",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("word counter", "count words", "word count"),
            badge = "LIVE"
        ),
        ToolDefinition(
            id = "text_char_counter",
            title = "Character Counter",
            description = "Character count with and without whitespace",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("character counter", "char count", "count letters")
        ),
        ToolDefinition(
            id = "text_sentence_counter",
            title = "Sentence Counter",
            description = "Sentence boundary analysis to check writing structure",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("sentence counter", "count sentences")
        ),
        ToolDefinition(
            id = "text_line_counter",
            title = "Line Counter",
            description = "Count total and non-empty lines in pasted text",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("line counter", "count lines")
        ),
        ToolDefinition(
            id = "text_reading_time",
            title = "Reading Time Calculator",
            description = "Estimated reading and speaking duration at standard wpm",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("reading time", "speaking time", "how long to read")
        ),
        ToolDefinition(
            id = "text_uppercase",
            title = "Uppercase Converter",
            description = "Convert all letters to UPPERCASE",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("uppercase", "all caps", "capitalize all")
        ),
        ToolDefinition(
            id = "text_lowercase",
            title = "Lowercase Converter",
            description = "Convert all letters to lowercase",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("lowercase", "small letters")
        ),
        ToolDefinition(
            id = "text_title_case",
            title = "Title Case Converter",
            description = "Capitalize First Letter Of Each Word",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("title case", "capitalize words")
        ),
        ToolDefinition(
            id = "text_sentence_case",
            title = "Sentence Case Converter",
            description = "Capitalize first letter of each sentence",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("sentence case", "capitalize sentences")
        ),
        ToolDefinition(
            id = "text_remove_spaces",
            title = "Remove Extra Spaces",
            description = "Collapse consecutive spaces and tabs into single spaces",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("remove spaces", "trim spaces", "clean spaces")
        ),
        ToolDefinition(
            id = "text_remove_dup_lines",
            title = "Remove Duplicate Lines",
            description = "Deduplicate repeating lines while keeping original order",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("remove duplicate lines", "deduplicate", "unique lines")
        ),
        ToolDefinition(
            id = "text_sort_lines",
            title = "Sort Lines",
            description = "Alphabetical and reverse alphabetical sorting of text lines",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("sort lines", "alphabetize", "sort a to z")
        ),
        ToolDefinition(
            id = "text_reverse",
            title = "Reverse Text",
            description = "Invert text strings and letters backwards",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("reverse text", "backwards text", "invert string")
        ),
        ToolDefinition(
            id = "text_find_replace",
            title = "Find & Replace",
            description = "Fast text search and string substitution",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("find and replace", "replace text", "substitute")
        ),
        ToolDefinition(
            id = "text_cleaner",
            title = "Text Cleaner",
            description = "Strip non-printable characters, HTML tags, and trailing spaces",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("text cleaner", "clean text", "strip html")
        ),
        ToolDefinition(
            id = "text_to_qr",
            title = "Text → QR",
            description = "Encode any written text or message into an offline QR code",
            category = ToolCategory.TEXT,
            route = "qr",
            keywords = listOf("text to qr", "qr text", "text qr code")
        ),
        ToolDefinition(
            id = "text_lorem_ipsum",
            title = "Lorem Ipsum Generator",
            description = "Generate standard placeholder text for mockups and designs",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("lorem ipsum", "dummy text", "placeholder text")
        ),
        ToolDefinition(
            id = "cool_fonts",
            title = "Cool Fonts & Bio Styler",
            description = "25+ aesthetic Unicode fonts, Gothic, Cursive, Small Caps & Kaomoji",
            category = ToolCategory.TEXT,
            route = "cool_fonts",
            keywords = listOf("font", "cool fonts", "fancy text", "bio fonts", "gothic", "cursive", "name font", "styler"),
            badge = "POPULAR"
        ),

        // ==========================================
        // 🔐 Category 10: PRIVACY & TECH (47 Tools)
        // ==========================================
        // QR & Barcode (161-170)
        ToolDefinition(
            id = "qr_scanner_tool",
            title = "QR Scanner",
            description = "Instant on-device camera scanner for QR codes",
            category = ToolCategory.TECH,
            route = "document_scanner",
            keywords = listOf("qr scanner", "scan qr", "read qr code"),
            badge = "CAMERA"
        ),
        ToolDefinition(
            id = "qr_generator_tool",
            title = "QR Generator",
            description = "Create customizable offline QR codes for links, text, Wi-Fi",
            category = ToolCategory.TECH,
            route = "qr",
            keywords = listOf("qr generator", "make qr", "create qr code"),
            badge = "OFFLINE"
        ),
        ToolDefinition(
            id = "qr_url",
            title = "URL → QR",
            description = "Create high-contrast QR codes for website URLs",
            category = ToolCategory.TECH,
            route = "qr",
            keywords = listOf("url to qr", "link to qr", "website qr")
        ),
        ToolDefinition(
            id = "qr_wifi",
            title = "Wi-Fi → QR",
            description = "Generate offline QR codes for instant WPA/WPA2 Wi-Fi connection",
            category = ToolCategory.TECH,
            route = "qr",
            keywords = listOf("wifi qr", "wifi qr code", "share wifi")
        ),
        ToolDefinition(
            id = "qr_contact",
            title = "Contact → QR",
            description = "Generate vCard QR codes to share contact details easily",
            category = ToolCategory.TECH,
            route = "qr",
            keywords = listOf("contact qr", "vcard qr", "business card qr")
        ),
        ToolDefinition(
            id = "qr_email",
            title = "Email → QR",
            description = "Create mailto QR codes with pre-filled subject and body",
            category = ToolCategory.TECH,
            route = "qr",
            keywords = listOf("email qr", "mailto qr")
        ),
        ToolDefinition(
            id = "qr_phone",
            title = "Phone → QR",
            description = "Generate tel QR codes for quick phone dialing",
            category = ToolCategory.TECH,
            route = "qr",
            keywords = listOf("phone qr", "dial qr", "call qr")
        ),
        ToolDefinition(
            id = "qr_text_tool",
            title = "Text → QR",
            description = "Encode notes, passwords, or text snippets into QR codes",
            category = ToolCategory.TECH,
            route = "qr",
            keywords = listOf("text to qr code", "message to qr")
        ),
        ToolDefinition(
            id = "barcode_scanner_tool",
            title = "Barcode Scanner",
            description = "Scan standard retail barcodes (EAN-13, UPC-A, Code-128)",
            category = ToolCategory.TECH,
            route = "document_scanner",
            keywords = listOf("barcode scanner", "scan barcode", "read barcode")
        ),
        ToolDefinition(
            id = "barcode_generator_tool",
            title = "Barcode Generator",
            description = "Render Code-128 and EAN barcodes offline",
            category = ToolCategory.TECH,
            route = "qr",
            keywords = listOf("barcode generator", "make barcode", "create barcode")
        ),

        // Privacy & Security (171-186)
        ToolDefinition(
            id = "pass_generator_tool",
            title = "Password Generator",
            description = "Generate cryptographically secure offline passwords with custom entropy",
            category = ToolCategory.TECH,
            route = "password_generator",
            keywords = listOf("password generator", "generate password", "secure password"),
            badge = "SECURE"
        ),
        ToolDefinition(
            id = "passphrase_gen",
            title = "Passphrase Generator",
            description = "Memorable Diceware-style multi-word passphrases",
            category = ToolCategory.TECH,
            route = "password_generator",
            keywords = listOf("passphrase", "diceware", "word password")
        ),
        ToolDefinition(
            id = "random_num_gen",
            title = "Random Number Generator",
            description = "True secure random number generator within min/max bounds",
            category = ToolCategory.TECH,
            route = "password_generator",
            keywords = listOf("random number", "rng", "random integer")
        ),
        ToolDefinition(
            id = "random_str_gen",
            title = "Random String Generator",
            description = "Alphanumeric random string generator for tokens and keys",
            category = ToolCategory.TECH,
            route = "password_generator",
            keywords = listOf("random string", "generate token", "random key")
        ),
        ToolDefinition(
            id = "uuid_gen",
            title = "UUID Generator",
            description = "Generate standard RFC 4122 Version 4 unique identifiers",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("uuid", "guid", "generate uuid", "v4 uuid")
        ),
        ToolDefinition(
            id = "hash_sha256_tool",
            title = "SHA-256 Hash",
            description = "Compute 256-bit cryptographic digest of text or passwords",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("sha256", "sha-256", "hash generator", "checksum")
        ),
        ToolDefinition(
            id = "hash_sha512_tool",
            title = "SHA-512 Hash",
            description = "High-security 512-bit cryptographic hash generation",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("sha512", "sha-512", "512 hash")
        ),
        ToolDefinition(
            id = "hash_md5_tool",
            title = "MD5 Hash",
            description = "Compute standard MD5 checksums for legacy file verification",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("md5", "md5 hash", "md5 checksum")
        ),
        ToolDefinition(
            id = "base64_enc",
            title = "Base64 Encoder",
            description = "Convert plain text to standard Base64 encoding",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("base64 encode", "text to base64")
        ),
        ToolDefinition(
            id = "base64_dec",
            title = "Base64 Decoder",
            description = "Decode Base64 encoded strings back to UTF-8 text",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("base64 decode", "base64 to text")
        ),
        ToolDefinition(
            id = "url_enc",
            title = "URL Encoder",
            description = "Percent-encode query parameters and URL paths",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("url encode", "percent encode")
        ),
        ToolDefinition(
            id = "url_dec",
            title = "URL Decoder",
            description = "Decode percent-encoded URLs and web query strings",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("url decode", "percent decode")
        ),
        ToolDefinition(
            id = "local_text_enc",
            title = "Local Text Encryption",
            description = "Encrypt sensitive notes offline with password-derived AES",
            category = ToolCategory.TECH,
            route = "password_generator",
            keywords = listOf("encrypt text", "aes encryption", "lock note")
        ),
        ToolDefinition(
            id = "local_text_dec",
            title = "Local Text Decryption",
            description = "Decrypt password-protected notes locally on device",
            category = ToolCategory.TECH,
            route = "password_generator",
            keywords = listOf("decrypt text", "unlock note")
        ),
        ToolDefinition(
            id = "file_hash_checker",
            title = "File Hash Checker",
            description = "Verify downloaded file integrity against published hashes",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("file hash", "verify file", "check sha256")
        ),
        ToolDefinition(
            id = "pass_strength_checker",
            title = "Password Strength Checker",
            description = "Evaluate entropy, crack time, and character diversity",
            category = ToolCategory.TECH,
            route = "password_generator",
            keywords = listOf("password strength", "crack time", "password score")
        ),

        // Internet/Tech Tools (187-206)
        ToolDefinition(
            id = "json_formatter_tool",
            title = "JSON Formatter",
            description = "Pretty-print indented JSON payloads with syntax error detection",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("json formatter", "format json", "pretty json")
        ),
        ToolDefinition(
            id = "json_minifier_tool",
            title = "JSON Minifier",
            description = "Strip whitespace and compact JSON to minimum byte footprint",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("json minifier", "compress json", "minify json")
        ),
        ToolDefinition(
            id = "xml_formatter_tool",
            title = "XML Formatter",
            description = "Format and indent XML tags and attributes cleanly",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("xml formatter", "format xml", "pretty xml")
        ),
        ToolDefinition(
            id = "html_formatter_tool",
            title = "HTML Formatter",
            description = "Format and clean messy HTML markup with proper nesting",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("html formatter", "format html")
        ),
        ToolDefinition(
            id = "html_escape_tool",
            title = "HTML Escape / Unescape",
            description = "Escape and unescape HTML entities (&lt;, &gt;, &amp;, &quot;)",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("html escape", "html entities", "unescape html")
        ),
        ToolDefinition(
            id = "url_parser_tool",
            title = "URL Parser",
            description = "Break down URLs into protocol, host, port, path, and query params",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("url parser", "parse url", "query params")
        ),
        ToolDefinition(
            id = "url_codec_tool",
            title = "URL Encoder / Decoder",
            description = "Full-duplex URL codec utility",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("url codec", "url encoder decoder")
        ),
        ToolDefinition(
            id = "jwt_decoder_tool",
            title = "JWT Decoder",
            description = "Inspect JSON Web Token headers, claims, and expiry dates offline",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("jwt", "jwt decoder", "decode jwt", "jwt token")
        ),
        ToolDefinition(
            id = "tech_unix_timestamp",
            title = "Unix Timestamp Converter",
            description = "Convert epoch seconds to human-readable date and time",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("unix time", "epoch converter", "timestamp")
        ),
        ToolDefinition(
            id = "regex_tester_tool",
            title = "Regex Tester",
            description = "Test regular expressions against text with real-time match groups",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("regex", "regular expression", "regex tester", "test pattern")
        ),
        ToolDefinition(
            id = "color_converter_tool",
            title = "Color Converter",
            description = "Convert colors seamlessly between HEX, RGB, and HSL",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("color converter", "hex to rgb", "rgb to hex", "hsl")
        ),
        ToolDefinition(
            id = "hex_to_rgb_tool",
            title = "HEX → RGB",
            description = "Convert 6-digit hex color codes (#3B82F6) to RGB values",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("hex to rgb", "hex color", "hex code")
        ),
        ToolDefinition(
            id = "rgb_to_hex_tool",
            title = "RGB → HEX",
            description = "Convert RGB values (59, 130, 246) to standard hex format",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("rgb to hex", "rgb color", "convert rgb")
        ),
        ToolDefinition(
            id = "hsl_converter_tool",
            title = "HSL Converter",
            description = "Convert colors to Hue, Saturation, and Lightness values",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("hsl", "hue saturation lightness", "hsl converter")
        ),
        ToolDefinition(
            id = "css_formatter_tool",
            title = "CSS Formatter",
            description = "Format and organize CSS stylesheets with uniform indentation",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("css formatter", "format css", "clean css")
        ),
        ToolDefinition(
            id = "tech_uuid_tool",
            title = "UUID Generator",
            description = "Rapidly generate batches of RFC 4122 v4 GUIDs",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("uuid generator", "make uuid", "guid")
        ),
        ToolDefinition(
            id = "user_agent_viewer",
            title = "User-Agent Viewer",
            description = "Inspect device browser identity and platform user agent string",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("user agent", "ua string", "browser agent")
        ),
        ToolDefinition(
            id = "ip_information",
            title = "IP Information",
            description = "View local network interface IP, subnet mask, and gateway",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("ip address", "local ip", "network ip")
        ),
        ToolDefinition(
            id = "dns_lookup_tool",
            title = "DNS Lookup",
            description = "Inspect local domain resolution and host names",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("dns lookup", "resolve host", "dns")
        ),
        ToolDefinition(
            id = "http_status_checker",
            title = "HTTP Status Checker",
            description = "Complete offline reference of HTTP 1xx-5xx status codes",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("http status", "404", "500", "200 ok", "http codes")
        ),

        // Color Tools (207-216)
        ToolDefinition(
            id = "color_picker",
            title = "Color Picker",
            description = "Visual color spectrum selector with instant HEX/RGB values",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("color picker", "pick color", "palette picker")
        ),
        ToolDefinition(
            id = "screen_color_picker",
            title = "Screen Color Picker",
            description = "Eyedropper utility to sample color codes",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("screen color", "eyedropper", "sample color")
        ),
        ToolDefinition(
            id = "color_hex_to_rgb",
            title = "HEX → RGB",
            description = "Precise hex to red, green, blue channel conversion",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("hex to rgb color", "hex code to rgb")
        ),
        ToolDefinition(
            id = "color_rgb_to_hex",
            title = "RGB → HEX",
            description = "Pack red, green, blue channels into standard hex format",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("rgb to hex color", "pack rgb")
        ),
        ToolDefinition(
            id = "color_rgb_to_hsl",
            title = "RGB → HSL",
            description = "Translate RGB color coordinates into cylindrical HSL values",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("rgb to hsl", "rgb hsl converter")
        ),
        ToolDefinition(
            id = "color_hsl_to_rgb",
            title = "HSL → RGB",
            description = "Calculate RGB color channels from HSL coordinates",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("hsl to rgb", "hsl rgb converter")
        ),
        ToolDefinition(
            id = "color_hex_to_hsl",
            title = "HEX → HSL",
            description = "Direct hexadecimal to HSL color conversion",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("hex to hsl", "hex hsl converter")
        ),
        ToolDefinition(
            id = "color_palette_gen",
            title = "Color Palette Generator",
            description = "Generate harmonious monochromatic and complementary palettes",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("color palette", "palette generator", "color schemes")
        ),
        ToolDefinition(
            id = "complementary_color",
            title = "Complementary Color",
            description = "Calculate exact 180° opposite complementary color",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("complementary color", "opposite color", "color contrast")
        ),
        ToolDefinition(
            id = "contrast_checker",
            title = "Contrast Checker",
            description = "Calculate WCAG 2.1 contrast ratios for text readability",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("contrast checker", "wcag contrast", "color accessibility")
        ),

        // Device Tools (217-227)
        ToolDefinition(
            id = "device_info",
            title = "Device Information",
            description = "Hardware brand, manufacturer, model, and board name",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("device info", "phone model", "manufacturer", "hardware")
        ),
        ToolDefinition(
            id = "android_version_info",
            title = "Android Version Information",
            description = "Android release version, API level, and security patch level",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("android version", "api level", "security patch")
        ),
        ToolDefinition(
            id = "screen_resolution",
            title = "Screen Resolution",
            description = "Native display width, height, and refresh rate in Hz",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("screen resolution", "display pixels", "refresh rate", "hz")
        ),
        ToolDefinition(
            id = "screen_density",
            title = "Screen Density",
            description = "Display DPI density bucket (mdpi, hdpi, xhdpi, xxhdpi)",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("screen density", "dpi", "display scale")
        ),
        ToolDefinition(
            id = "cpu_info",
            title = "CPU Information",
            description = "Processor architecture, ABI (arm64-v8a), and available CPU cores",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("cpu info", "processor", "cpu cores", "arm64")
        ),
        ToolDefinition(
            id = "ram_info",
            title = "RAM Information",
            description = "Device total RAM capacity and available memory stats",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("ram info", "memory", "ram size", "available ram")
        ),
        ToolDefinition(
            id = "storage_info",
            title = "Storage Information",
            description = "Internal storage size, available space, and file system stats",
            category = ToolCategory.TECH,
            route = "storage_cleaner",
            keywords = listOf("storage info", "internal storage", "disk capacity")
        ),
        ToolDefinition(
            id = "battery_info",
            title = "Battery Information",
            description = "Real-time battery percentage, health, charging status, and temp",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("battery info", "battery percentage", "battery health")
        ),
        ToolDefinition(
            id = "display_info",
            title = "Display Information",
            description = "Screen physical dimensions, orientation, and HDR support",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("display info", "hdr display", "screen dimensions")
        ),
        ToolDefinition(
            id = "network_info",
            title = "Network Information",
            description = "Active network connection type (Wi-Fi, Cellular, Ethernet)",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("network info", "wifi connection", "cellular status")
        ),
        ToolDefinition(
            id = "app_info",
            title = "App Information",
            description = "ONE app package name, version (1.0.8), build code, and target SDK",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("app info", "package name", "version 1.0.8")
        )
    )

    fun findByRoute(route: String): ToolDefinition? = tools.find { it.route == route }
    fun findByCategory(category: ToolCategory): List<ToolDefinition> = tools.filter { it.category == category }

    val sections = listOf(
        ToolSection(
            id = "images",
            title = "Images",
            icon = Icons.Outlined.PhotoLibrary,
            toolIds = listOf(
                "image_to_pdf", "multiple_images_to_pdf", "pdf_to_images", "image_compressor",
                "image_resizer", "image_converter", "image_cropper", "image_rotator",
                "image_flipper", "background_remover", "background_changer", "image_blur",
                "image_pixelator", "image_sharpen", "image_grayscale", "image_brightness",
                "image_contrast", "image_saturation", "image_metadata_viewer", "remove_image_metadata",
                "screenshot_cropper", "batch_image_compressor", "batch_image_resizer",
                "batch_image_converter", "batch_image_renamer"
            )
        ),
        ToolSection(
            id = "pdfs",
            title = "PDF Documents",
            icon = Icons.Outlined.PictureAsPdf,
            toolIds = listOf(
                "pdf_image_to_pdf", "pdf_merge", "pdf_split", "pdf_to_images_page",
                "pdf_compressor", "pdf_rotate", "pdf_reorder", "pdf_delete_pages",
                "pdf_extract_pages", "pdf_duplicate_pages", "pdf_page_counter",
                "pdf_add_text", "pdf_add_signature", "pdf_watermark", "pdf_metadata_viewer",
                "pdf_metadata_editor", "pdf_password_protect", "pdf_unlock",
                "pdf_page_size_converter", "pdf_scanner", "batch_pdf_processing"
            )
        ),
        ToolSection(
            id = "scanner",
            title = "Scanner & QR",
            icon = Icons.Outlined.DocumentScanner,
            toolIds = listOf(
                "scanner_doc", "scanner_id", "scanner_receipt", "scanner_receipt_card",
                "scanner_multi_page", "scanner_auto_edge", "scanner_perspective",
                "scan_to_pdf", "scan_to_image", "scanner_ocr", "ocr_to_text", "signature_capture"
            )
        ),
        ToolSection(
            id = "files",
            title = "Storage & Files",
            icon = Icons.Outlined.Folder,
            toolIds = listOf(
                "storage_analyzer", "large_file_finder", "duplicate_file_finder",
                "similar_image_finder", "screenshot_finder", "download_finder",
                "old_file_finder", "file_size_analyzer", "file_information", "file_renamer",
                "batch_file_renamer", "file_extension_changer", "zip_creator", "zip_extractor",
                "folder_size_analyzer", "temporary_file_cleaner", "empty_folder_finder"
            )
        ),
        ToolSection(
            id = "calculators",
            title = "Calculators",
            icon = Icons.Outlined.Calculate,
            toolIds = listOf(
                "calc_standard", "calc_scientific", "calc_percentage", "calc_discount",
                "calc_tax", "calc_tip", "calc_split_bill", "calc_profit", "calc_profit_margin",
                "calc_markup", "calc_simple_interest", "calc_compound_interest", "calc_loan",
                "calc_mortgage", "calc_emi", "calc_savings", "calc_investment_return",
                "calc_ratio", "calc_fraction", "calc_average", "calc_age", "calc_date_diff",
                "calc_time_diff", "calc_fuel_cost", "calc_speed", "calc_distance", "calc_bmi"
            )
        ),
        ToolSection(
            id = "converters",
            title = "Converters",
            icon = Icons.Outlined.SwapHoriz,
            toolIds = listOf(
                "conv_length", "conv_distance", "conv_area", "conv_volume", "conv_weight",
                "conv_mass", "conv_temp", "conv_speed", "conv_time", "conv_data_storage",
                "conv_energy", "conv_power", "conv_pressure", "conv_frequency", "conv_angle",
                "conv_fuel_economy", "conv_currency"
            )
        ),
        ToolSection(
            id = "money",
            title = "Money Tools",
            icon = Icons.Outlined.Paid,
            toolIds = listOf(
                "money_currency", "money_discount", "money_tax", "money_tip", "money_split_bill",
                "money_profit_loss", "money_markup", "money_margin", "money_loan_payment",
                "money_interest", "money_savings_goal", "money_compound_growth"
            )
        ),
        ToolSection(
            id = "date_time",
            title = "Date & Time",
            icon = Icons.Outlined.AccessTime,
            toolIds = listOf(
                "time_world_clock", "time_zone_converter", "time_date_calculator",
                "time_date_difference", "time_age_calculator", "time_countdown",
                "time_stopwatch", "time_timer", "time_alarm_shortcuts",
                "time_unix_timestamp", "time_week_number", "time_business_days"
            )
        ),
        ToolSection(
            id = "text",
            title = "Text Tools",
            icon = Icons.Outlined.FormatSize,
            toolIds = listOf(
                "text_word_counter", "text_char_counter", "text_sentence_counter",
                "text_line_counter", "text_reading_time", "text_uppercase", "text_lowercase",
                "text_title_case", "text_sentence_case", "text_remove_spaces",
                "text_remove_dup_lines", "text_sort_lines", "text_reverse", "text_find_replace",
                "text_cleaner", "text_to_qr", "text_lorem_ipsum", "cool_fonts"
            )
        ),
        ToolSection(
            id = "tech",
            title = "Privacy & Tech",
            icon = Icons.Outlined.Security,
            toolIds = listOf(
                "qr_scanner_tool", "qr_generator_tool", "qr_url", "qr_wifi", "qr_contact",
                "qr_email", "qr_phone", "qr_text_tool", "barcode_scanner_tool", "barcode_generator_tool",
                "pass_generator_tool", "passphrase_gen", "random_num_gen", "random_str_gen",
                "uuid_gen", "hash_sha256_tool", "hash_sha512_tool", "hash_md5_tool",
                "base64_enc", "base64_dec", "url_enc", "url_dec", "local_text_enc",
                "local_text_dec", "file_hash_checker", "pass_strength_checker",
                "json_formatter_tool", "json_minifier_tool", "xml_formatter_tool",
                "html_formatter_tool", "html_escape_tool", "url_parser_tool", "url_codec_tool",
                "jwt_decoder_tool", "tech_unix_timestamp", "regex_tester_tool", "color_converter_tool",
                "hex_to_rgb_tool", "rgb_to_hex_tool", "hsl_converter_tool", "css_formatter_tool",
                "tech_uuid_tool", "user_agent_viewer", "ip_information", "dns_lookup_tool",
                "http_status_checker", "color_picker", "screen_color_picker", "color_hex_to_rgb",
                "color_rgb_to_hex", "color_rgb_to_hsl", "color_hsl_to_rgb", "color_hex_to_hsl",
                "color_palette_gen", "complementary_color", "contrast_checker", "device_info",
                "android_version_info", "screen_resolution", "screen_density", "cpu_info",
                "ram_info", "storage_info", "battery_info", "display_info", "network_info", "app_info"
            )
        )
    )

    fun findSectionById(id: String): ToolSection? = sections.find { it.id == id }
    fun findTool(id: String): ToolDefinition? = tools.find { it.id == id }
}

data class ToolSection(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val toolIds: List<String>
) {
    fun getTools(): List<ToolDefinition> {
        return toolIds.mapNotNull { id -> ToolRegistry.tools.find { it.id == id } }
    }
}
