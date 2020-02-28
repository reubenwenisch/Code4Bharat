import cv2
import numpy as np
import math
import matplotlib.pyplot as plt

kSmoothFaceImage = False
kSmoothFaceFactor = 0.005
kEyePercentTop = 25
kEyePercentSide = 13
kEyePercentHeight = 30
kEyePercentWidth = 35
kFastEyeWidth = 50

color = "rgb"
bins = 10
lw = 3
alpha = 0.5
array = []

averageRold = [0,0,0]
averageLold = [0,0,0]

face_cascade = cv2.CascadeClassifier('/home/wenisch/OpenCV/opencv-3.0.0/data/haarcascades/haarcascade_frontalface_alt.xml')
#eye_cascade = cv2.CascadeClassifier('/home/wenisch/OpenCV/opencv-3.0.0/data/haarcascades/haarcascade_eye.xml')

if face_cascade.empty():
  raise IOError('Unable to load the face cascade classifier xml file')

cap = cv2.VideoCapture(0)

def detectAndDisplay(frame):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=2, flags = 0|cv2.CASCADE_SCALE_IMAGE|cv2.CASCADE_FIND_BIGGEST_OBJECT)
    for (x,y,w,h) in faces:
        cv2.rectangle(frame, (x,y), (x+w, y+h), (255,0,0), 2)
    if len(faces):
        findEyes(gray, faces[0])
    cv2.imshow('Face Detector', frame)

def findEyeCenter(faceROI,eyeRegion, eyeside):
    x_eye,y_eye,w_eye,h_eye = eyeRegion
    EyeROI = faceROI[y_eye:h_eye, x_eye:w_eye]
    #ret, thresh = cv2.threshold(EyeROI, 80, 255, cv2.THRESH_BINARY)
    thresh = cv2.adaptiveThreshold(EyeROI,255,cv2.ADAPTIVE_THRESH_GAUSSIAN_C,\
            cv2.THRESH_BINARY,11,2)
    threshold = thresh.mean(axis=0).mean(axis=0)
    cv2.imshow(eyeside, thresh)
    print(eyeside+"value = ", threshold)

def findintensity(gray,eyeRegion, text):
    x,y,w,h = eyeRegion
    numPixels = np.prod(gray.shape[:2])
    EyeROI = gray[y:y+h, x:x+w]
    average = EyeROI.mean(axis=0).mean(axis=0)
    #print(text+"value=",average)
    return average

def plot(data):
    array.append(data)
    plot(array)

def findEyes(gray, face):
    global averageRold
    global averageLold
    x,y,w,h = face
    faceROI = gray[y:y+h, x:x+w]
    debugFace = frame[y:y+h, x:x+w]
    debugFace_hsv = cv2.cvtColor(frame, cv2.COLOR_RGB2HSV)
    facewidth = w
    faceheight = h
    if kSmoothFaceImage == True:
        sigma = kSmoothFaceFactor * face.width
        blur = cv2.GaussianBlur(faceROI,(0,0),0)
    eye_region_width = int(facewidth * (kEyePercentWidth/100.0))
    Reye_region_width = int(facewidth * (kEyePercentWidth/100.0))+int(facewidth - eye_region_width - facewidth*(kEyePercentSide/100.0))
    eye_region_top = int(faceheight * (kEyePercentTop/100.0))
    eye_region_height = int(facewidth * (kEyePercentHeight/100.0))+eye_region_top
    LeftEyeRegion = [int(facewidth*(kEyePercentSide/100.0)),eye_region_top,eye_region_width+int(facewidth*(kEyePercentSide/100)),eye_region_height]
    cv2.rectangle(faceROI,(int(facewidth*(kEyePercentSide/100.0)),eye_region_top),(eye_region_width+int(facewidth*(kEyePercentSide/100)),eye_region_height),(255,0,0),2)
    print(LeftEyeRegion)
    cv2.rectangle(faceROI,(int(facewidth - eye_region_width -facewidth*(kEyePercentSide/100.0)),eye_region_top),(Reye_region_width,eye_region_height),(255,0,0),2)
    RightEyeRegion = [int(facewidth - eye_region_width -facewidth*(kEyePercentSide/100.0)),eye_region_top,Reye_region_width,eye_region_height]
    " Find the left Pupil"
    findEyeCenter(faceROI,RightEyeRegion,"Leye")
    findEyeCenter(faceROI,LeftEyeRegion,"Reye")
    #leftPupil = findEyeCenter(faceROI,leftEyeRegion,"Left Eye")
    #rightPupil = findEyeCenter(faceROI,rightEyeRegion,"Right Eye")
    " Find value of the ROI"
    #averageL = findintensity(debugFace,leftEyeRegion, "Left Eye")-averageLold
    #averageR = findintensity(debugFace,RightEyeRegion, "Right Eye")-averageRold
    #averageRold = averageR
    #averageLold = averageL
    #Rfft = np.fft.ifft(averageR)
    #Lfft = np.fft.ifft(averageL)
    #print("Left eye value=",averageL)
    #print("Right eye value=",averageR)
    cv2.imshow('FaceROI', faceROI)

#def computeMatXGradient(eyeROI):


def findEyeCenter2(face,leftEyeRegion):
    x,y,w,h = leftEyeRegion
    eyeROIUnscaled = face[y:y+h, x:x+w]
    eyeROI = cv2.resize(eyeROIUnscaled,(kFastEyeWidth,int(((kFastEyeWidth)/h) * w)))
    laplace = cv2.Laplacian(eyeROIUnscaled,cv2.CV_64F)
    cv2.imshow('Laplacian', laplace)

# Initialize plot.
def init_plot():
    fig, ax = plt.subplots()
    if color == 'rgb':
        ax.set_title('Histogram (RGB)')
    else:
        ax.set_title('Histogram (grayscale)')
    ax.set_xlabel('Frequency')
    ax.set_ylabel('Intensity')
    plt.show()

#init_plot()
while True:
    ret, frame = cap.read()
    detectAndDisplay(frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()

# https://github.com/trishume/eyeLike/tree/master/src
