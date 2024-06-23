import face_recognition
import os
import sys
import cv2
import numpy as np
import math
import pandas as pd
import time
from openpyxl import load_workbook
import mysql.connector
from datetime import date
from email.message import EmailMessage
import ssl
import smtplib
import winsound
from datetime import datetime



frequency = 2500  # Set Frequency To 2500 Hertz
duration = 250  # Set Duration To 1000 ms == 1 second
#initializes the list of students
studentDB = pd.read_excel('Students.xlsx')



def sendemail(reciever,fullname,current_time):
    # Define email sender and receiver
    email_sender = ''
    email_password = ''
 
    # Set the subject and body of the email
    subject = f'{fullname} has arrived at school'
    body = f'{fullname} has arrived at APC safely at {current_time}'
    print("Message sent")
 
    em = EmailMessage()
    em['From'] = email_sender
    em['To'] = reciever
    em['Subject'] = subject
    em.set_content(body)
 
    # Add SSL (layer of security)
    context = ssl.create_default_context()
 
    # Log in and send the email
    with smtplib.SMTP_SSL('smtp.gmail.com', 465, context=context) as smtp:
        smtp.login(email_sender, email_password)
        smtp.sendmail(email_sender, reciever, em.as_string())

#connects to the mySQL database
def connect_to_db():
    connection = mysql.connector.connect(
        host="",
        user="",
        password="",
        database=""
    )
    return connection
today = time.strftime('%Y_%m_%d')
table_name = f"attendance_{today}"

#creates the table if a table hasn't been created for that day
def create_daily_attendance_table(connection,table_name):
    cursor = connection.cursor()
    # Create table if it does not exist
    create_table_query = f"""
    CREATE TABLE IF NOT EXISTS {table_name} (
        student_id VARCHAR(255) NOT NULL,
        first_name VARCHAR(255) NOT NULL,
        last_name VARCHAR(255) NOT NULL,
        section VARCHAR(255) NOT NULL,
        date VARCHAR(255) NOT NULL,
        time VARCHAR(255) NOT NULL,
        status VARCHAR(255) NOT NULL,
        PRIMARY KEY (student_id)
    );
    """
    cursor.execute(create_table_query)
    connection.commit()
    cursor.close()
    
    return table_name


def create_attendance_record(connection, table_name, student_id, first_name, last_name, section, datetoday,current_time):
    cursor = connection.cursor()
    query = f"""
    INSERT INTO {table_name} (student_id, first_name, last_name, section, date,time, status)
    VALUES (%s, %s, %s, %s, %s, %s, %s)
    ON DUPLICATE KEY UPDATE
        status = VALUES(status)
    """
    data = (student_id, first_name, last_name, section, datetoday,current_time,"PRESENT")
    cursor.execute(query, data)
    connection.commit()
    cursor.close()


def writetomaster(connection, student_id, first_name, last_name, section, datetoday,current_time):
    cursor = connection.cursor()
 
    query = """
    INSERT INTO masterattendance (student_id, first_name, last_name, section, date, time)
    VALUES (%s, %s, %s, %s, %s, %s)
    """
    data = (student_id, first_name, last_name, section, datetoday, current_time)
    try:
        cursor.execute(query, data)
        connection.commit()
    except Exception as e:
        print(f"Error: {e}")
        connection.rollback()
    finally:
        cursor.close()


def logact(connection, student_id, first_name, last_name, activity, datetoday, current_time,section):
    cursor = connection.cursor()

    query = """
    INSERT INTO logs (student_id, first_name, last_name,section, activity, date, time)
    VALUES (%s, %s, %s, %s, %s,%s, %s)
    """

    data = (student_id, first_name, last_name, section, activity, datetoday, current_time)

    try:
        cursor.execute(query, data)
        connection.commit()
    except Exception as e:
        print(f"Error: {e}")
        connection.rollback()
    finally:
        cursor.close()




#calculates face confidence level
def face_confidence(face_distance, face_match_threshold=0.6):
    if face_distance > face_match_threshold:
        range = (1.0 - face_match_threshold)
        linear_value = (1.0 - face_distance) / (range * 2.0)
        return str(round(linear_value * 100, 2)) + "%"
    else:
        range = (1.0 - face_match_threshold)
        linear_value = (1.0 - face_distance) / (range * 2.0)
        value = (linear_value + ((1.0 - linear_value) * math.pow((linear_value - 0.5) * 2, 0.2))) * 100
        return str(round(value, 2)) + "%"
    
def testprint(name,fullname):
    try:
        print(fullname)
    except:
        print(f"No record found for student ID: {name}")

# Initialize cachedname as None
cachedname = None
import time

# Dictionary to store the last detection time for each student
last_detection_time = {}

def features(name, table_name):
    global cachedname  # Declare cachedname as global to modify it

    if name != cachedname:
        connection = connect_to_db()
        # Create the specific table if it doesn't exist
        create_daily_attendance_table(connection, table_name)
        searchid = name
        student_record = studentDB[studentDB['studentid'] == searchid]
                            
        if not student_record.empty:
            student_id = student_record['studentid'].iloc[0]
            first_name = student_record['First_name'].iloc[0]
            last_name = student_record['Last_name'].iloc[0]
            section = student_record['Section'].iloc[0]
            reciever = student_record['parent_email'].iloc[0]
            fullname = first_name + " " + last_name  # Added space between first and last name
            datetoday = date.today().strftime('%B %d %Y')
            current_time = datetime.now().strftime('%I:%M %p')

            # Check if cooldown period has elapsed since the last detection
            cooldown_elapsed = False
            if student_id in last_detection_time:
                last_time = last_detection_time[student_id]
                if time.time() - last_time >= 4:  # 2 seconds cooldown period
                    cooldown_elapsed = True
            else:
                cooldown_elapsed = True

            if cooldown_elapsed:
                # Check if student is already present
                cursor = connection.cursor()
                query = f"SELECT * FROM {table_name} WHERE student_id = '{student_id}'"
                cursor.execute(query)
                result = cursor.fetchone()
                cursor.close()

                if result:  
                    current_status = result[6]  # Get current status from the database
                    new_status = None  # Initialize new_status variable

                    if current_status == "PRESENT":
                        new_status = "OFF CAMPUS"
                        logact(connection, student_id, first_name, last_name, "EXITED", datetoday, current_time, section)
                    elif current_status == "OFF CAMPUS":
                        new_status = "PRESENT"
                        logact(connection, student_id, first_name, last_name, "ENTERED", datetoday, current_time, section)

                    if new_status is not None and new_status != current_status:
                        # Update status only if it's different from the current status
                        cursor = connection.cursor()
                        update_query = f"UPDATE {table_name} SET status = '{new_status}' WHERE student_id = '{student_id}'"
                        cursor.execute(update_query)
                        connection.commit()
                        cursor.close()
                        print(f"{fullname} is now {new_status.replace('_', ' ').lower()}.")

                else:
                    # If student not present, add new record
                    create_attendance_record(connection, table_name, student_id, first_name, last_name, section, datetoday, current_time)
                    sendemail(reciever, fullname, current_time)
                    testprint(name, fullname)
                    writetomaster(connection, student_id, first_name, last_name, section, datetoday, current_time)
                    cachedname = name  # Update cachedname
                    winsound.Beep(frequency, duration)

                # Update last detection time
                last_detection_time[student_id] = time.time()
            else:
                print("Cooldown period active. Skipping status update.")

    else:
        print("Student already recorded")






#main feature
class FaceRecog:
    def __init__(self):
        self.face_locations = []
        self.face_encodings = []
        self.face_names = []
        self.known_face_encodings = []
        self.known_face_names = []
        self.process_this_frame = True
        self.encode_faces()
        self.last_testprint_time = time.time()
    def encode_faces(self):
        for image in os.listdir('facedb'):
            face_image = face_recognition.load_image_file(f'facedb/{image}')
            face_encoding = face_recognition.face_encodings(face_image)[0]

            self.known_face_encodings.append(face_encoding)
            self.known_face_names.append(os.path.splitext(image)[0])

        print(self.known_face_names)

    def run_recog(self):
        video_capture = cv2.VideoCapture(0)

        if not video_capture.isOpened():
            sys.exit("Video source not found")


        while True:

            # Grab a single frame of video
            ret, frame = video_capture.read()

            # Only process every other frame of video to save time
            if self.process_this_frame:
                # Resize frame of video to 1/4 size for faster face recognition processing
                small_frame = cv2.resize(frame, (0, 0), fx=0.25, fy=0.25)

                # Convert the image from BGR color (which OpenCV uses) to RGB color (which face_recognition uses)
                rgb_small_frame = np.ascontiguousarray(small_frame[:, :, ::-1])
                
                # Find all the faces and face encodings in the current frame of video
                self.face_locations = face_recognition.face_locations(rgb_small_frame)
                self.face_encodings = face_recognition.face_encodings(rgb_small_frame, self.face_locations)

                self.face_names = []
                self.face_confidences = []
                for face_encoding in self.face_encodings:
                    # See if the face is a match for the known face(s)
                    matches = face_recognition.compare_faces(self.known_face_encodings, face_encoding)
                    name = "Unknown"
                    confidence = "???"

                    # Or instead, use the known face with the smallest distance to the new face
                    face_distances = face_recognition.face_distance(self.known_face_encodings, face_encoding)
                    best_match_index = np.argmin(face_distances)
                    
                    if matches[best_match_index]:
                        if float(face_confidence(face_distances[best_match_index]).rstrip('%')) > 96:  # Check if c
                            name = self.known_face_names[best_match_index]
                            confidence = face_confidence(face_distances[best_match_index])

                        self.face_names.append(name)
                        self.face_confidences.append(confidence)

                        features(name,table_name)

                    


            self.process_this_frame = not self.process_this_frame
            # Display the results
            for (top, right, bottom, left), name, confidence in zip(self.face_locations, self.face_names, self.face_confidences):
                # Scale back up face locations since the frame we detected in was scaled to 1/4 size
                top *= 4
                right *= 4
                bottom *= 4
                left *= 4

                # Draw a box around the face
                cv2.rectangle(frame, (left, top), (right, bottom), (0, 0, 255), 2)

                # Draw a label with a name and confidence below the face
                cv2.rectangle(frame, (left, bottom - 50), (right, bottom), (0, 0, 255), cv2.FILLED)
                font = cv2.FONT_HERSHEY_DUPLEX
                cv2.putText(frame, f"{name} ({confidence})", (left + 6, bottom - 6), font, 1.0, (255, 255, 255), 1)

            # Display the resulting image
            cv2.imshow('Face recognition', frame)
            
            # Hit 'q' on the keyboard to quit!
            if cv2.waitKey(1) & 0xFF == ord('q'):       
                break


        



        # Release handle to th
        # e webcam
        video_capture.release()
        cv2.destroyAllWindows()

if __name__ == '__main__':
    fr = FaceRecog()
    fr.run_recog()