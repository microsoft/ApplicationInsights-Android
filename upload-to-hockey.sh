# Sets the target folders and the final framework product.
FMK_NAME=applicationinsights-android
FMK_VERSION=A

# Install dir will be the final output to the framework.
# The following line creates it in the root folder of the current project.
PRODUCTS_DIR=${WORKSPACE}/Products
ZIP_FOLDER=applicationinsights-android
TEMP_DIR=${PRODUCTS_DIR}/${ZIP_FOLDER}

#create Products-Folder
mkdir -p "$PRODUCTS_DIR"
mkdir -p "$TEMP_DIR"
#Copy licence file
cp -f "README.md" "$TEMP_DIR"
#copy aar to directory
cp -f "applicationinsights-android/build/outputs/aar/applicationinsights-android-release.aar" "$TEMP_DIR"
#create zip file for upload
cd "${PRODUCTS_DIR}"
rm -f "${FMK_NAME}-daily.zip"
zip -yr "${FMK_NAME}-daily.zip" "${ZIP_FOLDER}" -x \*/.*

#upload to hockeyapp
/usr/local/bin/puck -submit=auto -download=true -collect_notes_type=jenkins_aggregate -collect_notes_path="${WORKSPACE}" -notes_type=markdown -source_path="${WORKSPACE}" -repository_url="https://github.com/Microsoft/AppInsights-Android.git" -bundle_version="AppInsights-Android-1.0-a.1" -api_token=cffadefa97534ff2b38894ed025e3e76 -app_id=ebd7b100a63fe2348e72a7f2aefe892d "${PRODUCTS_DIR}/${FMK_NAME}-daily.zip"
