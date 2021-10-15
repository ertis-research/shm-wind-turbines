import pickle
import numpy as np
from os.path import dirname, join
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import load_img
from tensorflow.keras.preprocessing.image import img_to_array

# load model
model = load_model(join(dirname(__file__), "model2.h5"))

# load label binarizer
lb = pickle.loads(open(join(dirname(__file__), "lb2.pickle"), "rb").read())

def predict(imagePath):
   # load the input image (in Keras format) from disk and preprocess it,
   # scaling the pixel intensities to the range [0, 1]
   image = load_img(imagePath, target_size=(224, 224))
   image = img_to_array(image) / 255.0
   image = np.expand_dims(image, axis=0)

   # predict the bounding box of the object along with the class label
   (boxPreds, labelPreds) = model.predict(image)

   # determine the class label with the largest predicted probability
   i = np.argmax(labelPreds, axis=1)
   label = lb.classes_[i][0]

   return label