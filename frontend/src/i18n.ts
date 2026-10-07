import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// Translation files
const resources = {
  en: {
    translation: {
      "welcome": "Welcome to SmartSpace",
      "booking.title": "Book Hall",
      "booking.step1": "1. Time",
      "booking.step2": "2. Details",
      "booking.step3": "3. Review",
      "booking.date": "Date",
      "booking.startTime": "Start Time",
      "booking.endTime": "End Time",
      "booking.guests": "Guests",
      "booking.next": "Next",
      "booking.back": "Back",
      "booking.confirm": "Confirm Booking",
      "booking.cancel": "Cancel",
      "privacy.dashboard": "Privacy Dashboard",
      "privacy.export": "Export My Data",
      "privacy.delete": "Request Account Deletion",
      "watchman.approved": "Entry Approved",
      "watchman.denied": "Entry Denied"
    }
  },
  hi: {
    translation: {
      "welcome": "स्मार्टस्पेस में आपका स्वागत है",
      "booking.title": "हॉल बुक करें",
      "booking.step1": "1. समय",
      "booking.step2": "2. विवरण",
      "booking.step3": "3. समीक्षा",
      "booking.date": "तारीख",
      "booking.startTime": "शुरुआत का समय",
      "booking.endTime": "समाप्ति का समय",
      "booking.guests": "अतिथि",
      "booking.next": "आगे",
      "booking.back": "पीछे",
      "booking.confirm": "बुकिंग की पुष्टि करें",
      "booking.cancel": "रद्द करें",
      "privacy.dashboard": "गोपनीयता डैशबोर्ड",
      "privacy.export": "मेरा डेटा निर्यात करें",
      "privacy.delete": "खाता हटाने का अनुरोध करें",
      "watchman.approved": "प्रवेश स्वीकृत",
      "watchman.denied": "प्रवेश अस्वीकृत"
    }
  },
  mr: {
    translation: {
      "welcome": "स्मार्टस्पेस मध्ये आपले स्वागत आहे",
      "booking.title": "हॉल बुक करा",
      "booking.step1": "1. वेळ",
      "booking.step2": "2. तपशील",
      "booking.step3": "3. पुनरावलोकन",
      "booking.date": "तारीख",
      "booking.startTime": "सुरुवातीची वेळ",
      "booking.endTime": "समाप्तीची वेळ",
      "booking.guests": "पाहुणे",
      "booking.next": "पुढे",
      "booking.back": "मागे",
      "booking.confirm": "बुकिंगची पुष्टी करा",
      "booking.cancel": "रद्द करा",
      "privacy.dashboard": "गोपनीयता डॅशबोर्ड",
      "privacy.export": "माझा डेटा निर्यात करा",
      "privacy.delete": "खाते हटविण्याची विनंती करा",
      "watchman.approved": "प्रवेश मंजूर",
      "watchman.denied": "प्रवेश नाकारला"
    }
  }
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false // react already safes from xss
    }
  });

export default i18n;
