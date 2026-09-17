package com.one.utility.core.router

enum class ToolCategory(val title: String) {
    IMAGES("Images"),
    PDF("PDFs"),
    CALCULATOR("Calculators"),
    SCAN_QR("QR & Barcode"),
    FILES("Files"),
    TEXT("Text"),
    TECH("Tech")
}

data class ToolDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val route: String,
    val keywords: List<String>,
    val badge: String? = null
)

object ToolRegistry {
    val tools = listOf(
        ToolDefinition(
            id = "image_to_pdf",
            title = "Image → PDF",
            description = "Combine photos into clean A4 or Letter PDF with margins",
            category = ToolCategory.IMAGES,
            route = "image_to_pdf",
            keywords = listOf("image", "photo", "picture", "pdf", "photos to pdf", "turn these photos into a pdf", "make a pdf"),
            badge = "FAST LOCAL"
        ),
        ToolDefinition(
            id = "background_remover",
            title = "Remove Background",
            description = "On-device AI cutout with transparent or solid background",
            category = ToolCategory.IMAGES,
            route = "background_remover",
            keywords = listOf("remove background", "cutout", "transparent bg", "no background", "remove bg", "background removal"),
            badge = "ON-DEVICE"
        ),
        ToolDefinition(
            id = "image_compressor",
            title = "Compress Image",
            description = "Reduce file size up to 85% without noticeable quality loss",
            category = ToolCategory.IMAGES,
            route = "compressor",
            keywords = listOf("compress", "smaller", "shrink", "reduce size", "make this image smaller", "compression"),
            badge = "BATCH"
        ),
        ToolDefinition(
            id = "image_resizer",
            title = "Resize Image",
            description = "Resize dimensions to 1080px, 1920px or custom aspect ratios",
            category = ToolCategory.IMAGES,
            route = "resizer",
            keywords = listOf("resize", "dimensions", "1080px", "1920px", "aspect ratio", "scale")
        ),
        ToolDefinition(
            id = "calculator",
            title = "Keypad & Scientific Calculator",
            description = "Tactile keypad arithmetic, trig, log, powers, and 1-tap scientific switch",
            category = ToolCategory.CALCULATOR,
            route = "calculator",
            keywords = listOf("calc", "calculate", "scientific", "trig", "sin", "cos", "keypad", "math")
        ),
        ToolDefinition(
            id = "unit_converter",
            title = "Scientific Unit Converter",
            description = "200+ units across Astronomy, Physics, CS, Chemistry, Mechanics, Energy",
            category = ToolCategory.CALCULATOR,
            route = "unit_converter",
            keywords = listOf("convert", "miles to km", "light year", "astronomy", "physics", "units", "converter")
        ),
        ToolDefinition(
            id = "qr_generator",
            title = "QR Code Generator",
            description = "Create customizable offline QR codes for links, text, Wi-Fi",
            category = ToolCategory.SCAN_QR,
            route = "qr",
            keywords = listOf("qr", "qr code", "generate qr", "make qr", "wifi qr", "barcode")
        ),
        ToolDefinition(
            id = "pdf_toolbox",
            title = "PDF Toolbox",
            description = "Merge multiple PDFs, convert PDF to images, or split pages",
            category = ToolCategory.PDF,
            route = "pdf_toolbox",
            keywords = listOf("merge pdf", "pdf to image", "split pdf", "combine pdf", "pdf pages"),
            badge = "LOCAL"
        ),
        ToolDefinition(
            id = "text_tools",
            title = "Text Analyzer & Tools",
            description = "Word count, formatting, uppercase/lowercase, deduplicate lines",
            category = ToolCategory.TEXT,
            route = "text_tools",
            keywords = listOf("word count", "character count", "uppercase", "lowercase", "text cleaner")
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
        ToolDefinition(
            id = "dev_tools",
            title = "Developer Tools",
            description = "JSON formatter, Base64 encode/decode, JWT, SHA hashes, Unix time",
            category = ToolCategory.TECH,
            route = "dev_tools",
            keywords = listOf("json", "base64", "jwt", "sha-256", "hash", "unix", "timestamp"),
            badge = "DEV"
        ),
        ToolDefinition(
            id = "password_generator",
            title = "Password Generator",
            description = "Generate cryptographically secure offline passwords",
            category = ToolCategory.TECH,
            route = "password_generator",
            keywords = listOf("password", "generate password", "secure password", "random string")
        ),
        ToolDefinition(
            id = "storage_cleaner",
            title = "Storage Cleaner",
            description = "Analyze disk space, find large files and duplicate content",
            category = ToolCategory.FILES,
            route = "storage_cleaner",
            keywords = listOf("storage", "clean storage", "duplicate photos", "large files", "cleaner"),
            badge = "CLEAN"
        ),
        ToolDefinition(
            id = "batch_rename",
            title = "Batch File Renamer",
            description = "Rename files in bulk using pattern search and sequential numbering",
            category = ToolCategory.FILES,
            route = "batch_rename",
            keywords = listOf("rename", "batch rename", "rename files", "renamer")
        ),
        ToolDefinition(
            id = "image_cropper",
            title = "Crop & Rotate",
            description = "Free crop, 1:1, 16:9 aspect ratios, 90° rotation and horizontal flip",
            category = ToolCategory.IMAGES,
            route = "image_cropper",
            keywords = listOf("crop", "rotate", "flip", "aspect ratio", "crop image")
        ),
        ToolDefinition(
            id = "document_scanner",
            title = "Document Scanner",
            description = "Scan paper notes and documents with crisp B&W and color enhancement",
            category = ToolCategory.SCAN_QR,
            route = "document_scanner",
            keywords = listOf("scan document", "document scanner", "scan", "paper scan", "bw document"),
            badge = "SCAN"
        ),
        ToolDefinition(
            id = "everyday_calculators",
            title = "Everyday Calculators",
            description = "Profit margin, sales tax/VAT, compound interest, BMI, and trip fuel",
            category = ToolCategory.CALCULATOR,
            route = "everyday_calculators",
            keywords = listOf("margin", "profit", "markup", "tax", "vat", "interest", "bmi", "fuel cost")
        ),
        ToolDefinition(
            id = "image_converter",
            title = "Image Converter",
            description = "Convert between PNG, JPG, and WEBP formats with quality control",
            category = ToolCategory.IMAGES,
            route = "image_converter",
            keywords = listOf("convert image", "png to jpg", "jpg to png", "convert webp", "image format"),
            badge = "CONVERT"
        ),
        ToolDefinition(
            id = "currency_time",
            title = "Currency & World Time",
            description = "Offline currency converter with custom rates & world timezone clock",
            category = ToolCategory.CALCULATOR,
            route = "currency_time",
            keywords = listOf("currency", "exchange rate", "time zone", "world clock", "lagos to tokyo")
        )
    )

    fun findByRoute(route: String): ToolDefinition? = tools.find { it.route == route }
    fun findByCategory(category: ToolCategory): List<ToolDefinition> = tools.filter { it.category == category }
}
