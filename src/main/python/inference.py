import cv2
import pickle
import numpy as np
from os.path import dirname, join
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import load_img
from tensorflow.keras.preprocessing.image import img_to_array

# load model
model = load_model(join(dirname(__file__), "model.h5"))

# load label binarizer
lb = pickle.loads(open(join(dirname(__file__), "lb.pickle"), "rb").read())

def predict(imagePath):
   # open, filter and save image
   img = cv2.imread(imagePath)
   grayscale = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
   _, threshold = cv2.threshold(grayscale, 0, 255, cv2.THRESH_OTSU)
   bbox = cv2.boundingRect(threshold)
   x, y, w, h = bbox
   foreground = img[y:y+h, x:x+w]
   cv2.imwrite(imagePath, foreground)

   # get image size
   (height, width) = foreground.shape[:2]

   # load the input image (in Keras format) from disk and preprocess it,
   # scaling the pixel intensities to the range [0, 1]
   image = load_img(imagePath, target_size=(224, 224))
   image = img_to_array(image) / 255.0
   image = np.expand_dims(image, axis=0)

   # predict the bounding box of the object along with the class label
   (boxPreds, labelPreds) = model.predict(image)
   (startX, startY, endX, endY) = boxPreds[0]

   # determine the class label with the largest predicted probability
   i = np.argmax(labelPreds, axis=1)
   label = lb.classes_[i][0]

   # scale the predicted bounding box coordinates based on the image dimensions
   startX = int(round(startX * width))
   startY = int(round(startY * height))
   endX = int(round(endX * width))
   endY = int(round(endY * height))

   result = [label, [width, height], [startX, startY, endX, endY]]

   return result