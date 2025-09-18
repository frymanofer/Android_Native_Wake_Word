
# Navigate to the directory containing your .aar and .pom files
cd android/libs/com/davoice/keyworddetection/1.0.0/

# Generate checksums for the .aar file
md5sum keyworddetection-1.0.0.aar > keyworddetection-1.0.0.aar.md5
sha1sum keyworddetection-1.0.0.aar > keyworddetection-1.0.0.aar.sha1

# Generate checksums for the .pom file
md5sum keyworddetection-1.0.0.pom > keyworddetection-1.0.0.pom.md5
sha1sum keyworddetection-1.0.0.pom > keyworddetection-1.0.0.pom.sha1

cd -

# Navigate to the directory containing your .aar and .pom files
cd android_min_footprint/libs/com/davoice/keyworddetection/1.0.0/

# Generate checksums for the .aar file
md5sum keyworddetection-1.0.0.aar > keyworddetection-1.0.0.aar.md5
sha1sum keyworddetection-1.0.0.aar > keyworddetection-1.0.0.aar.sha1

# Generate checksums for the .pom file
md5sum keyworddetection-1.0.0.pom > keyworddetection-1.0.0.pom.md5
sha1sum keyworddetection-1.0.0.pom > keyworddetection-1.0.0.pom.sha1

cd -