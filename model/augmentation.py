import config
import albumentations as A
import imutils
from imutils import paths
import cv2
import csv
import os

# initialize the list of data (images) and target bounding box coordinates
print("[INFO] Preparing data...")
imageList = []
bboxList = []
imageNameList = []

# loop over all CSV files in the annotations directory
for csvPath in paths.list_files(config.ANNOTS_PATH, validExts=(".csv")):
    # load the contents of the current CSV annotations file
    rows = open(csvPath).read().strip().split("\n")
    # loop over the rows
    for row in rows:
        # break the row into the filename, shape, class label and bounding box coordinates
        row = row.split(",")
        (filename, width, height, label, startX, startY, endX, endY) = row

        # derive the path to the input image and load the image (in OpenCV format)
        imagePath = os.path.sep.join([config.IMAGES_PATH, label, filename])
        image = cv2.imread(imagePath)
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        # update our list of images and bounding boxes
        imageList.append(image)
        imageNameList.append(filename)
        bbox = []
        coor = [int(startX), int(startY), int(endX), int(endY), label]
        bbox.append(coor)
        bboxList.append(bbox)

# define an augmentation pipeline
transform = A.Compose([
    A.Flip(p=1.0),
    A.ChannelShuffle(p=1.0),
], bbox_params=A.BboxParams(format='pascal_voc'))

transformedImageList = []
transformedBboxList = []

# loop over our list of images and bounding boxes to apply the transformations
print("[INFO] Applying transformations...")
for image, bbox in zip(imageList, bboxList):
    transformed = transform(image=image, bboxes=bbox)
    transformed_image = transformed['image']
    transformed_bboxes = transformed['bboxes']

    transformedImageList.append(transformed_image)
    transformedBboxList.append(transformed_bboxes)

# save the new images and bounding boxes
print("[INFO] Saving new dataset...")
path_to_save = '.\datatransformations'
path_to_csv = os.path.join(path_to_save, 'transformed_bboxes.csv')

with open(path_to_csv, mode='w', newline="") as transformed_file:
    transformed_writer = csv.writer(transformed_file, delimiter=',')

    for name, image, bbox in zip(imageNameList, transformedImageList, transformedBboxList):
        # save the image
        new_name = 'transformed' + name
        path_to_image = os.path.join(path_to_save, new_name)
        cv2.imwrite(path_to_image, image)

        # save the bounding boxes
        new_coord = bbox[0]
        new_startX = new_coord[0]
        new_startY = new_coord[1]
        new_endX = new_coord[2]
        new_endY = new_coord[3]
        label = new_coord[4]

        # cast the transformed bounding box coordinates from float to int
        new_startX = int(round(new_startX))
        new_startY = int(round(new_startY))
        new_endX = int(round(new_endX))
        new_endY = int(round(new_endY))

        transformed_writer.writerow([new_name, '1024', '1024', label, new_startX, new_startY, new_endX, new_endY])
        
        # test the new output
        # load the image (in OpenCV format)
        img = cv2.imread(path_to_image)

        # draw the transformed bounding box and class label on the image
        y = new_startY - 10 if new_startY - 10 > 10 else new_startY + 10
        cv2.putText(img, label, (new_startX, y), cv2.FONT_HERSHEY_SIMPLEX, 0.65, (0, 255, 0), 2)
        cv2.rectangle(img, (new_startX, new_startY), (new_endX, new_endY), (0, 255, 0), 2)

        # show the output image
        cv2.imshow("Output", img)
        cv2.waitKey(0)