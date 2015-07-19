package com.worldspotlightapp.android.maincontroller.modules;

import android.content.Context;
import android.util.Log;

import com.parse.ParseException;
import com.worldspotlightapp.android.R;

/**
 * This class pretends to provide customized messages
 * that Parse does not provide, such as login with inexistent user,
 * which the error code provided by Parse is OBJECT_NOT_FOUND.
 * Created by Jiahao on 5/27/15.
 */
public class ParseResponse extends ParseException {

    private static final String TAG = "ParseResponse";

    /**
     * The request code for the correct request. This number is big enough
     * to not coincide with the Parse error codes
     */
    public static final int REQUEST_CORRECT = 10000;

    /**
     * Error if the video is not found
     */
    public static final int ERROR_VIDEO_NOT_FOUND = 10001;

    /**
     * Generic error login with with facebook
     */
    public static final int ERROR_LOGIN_WITH_FACEBOOK = 10002;

    /**
     * Generic error login with with facebook
     */
    public static final int ERROR_LOGIN_WITH_PARSE = 10003;

    /**
     * Generic error login with Google Plus
     */
    public static final int ERROR_LOGIN_WITH_GOOGLE = 10004;

    /**
     * Generic error when an operation require the user to logged
     * in but he is not
     */
    public static final int ERROR_USER_NOT_LOGGED_IN = 10005;

    // Status code
    // This should be the same as ParseException.
    // Since the variable code from the class ParseException is private
    // and it does not provide any method to set this field, a new
    // field must be created in this class
    public int mStatusCode = REQUEST_CORRECT;

    private ParseResponse() {
        super(REQUEST_CORRECT, "The request was correct");
    }

    private ParseResponse(int theCode) {
        this();
        this.mStatusCode = theCode;
    }

    private ParseResponse(int theCode, String theMessage) {
        super(theCode, theMessage);
        this.mStatusCode = theCode;
    }

    private ParseResponse(int theCode, String message, Throwable cause) {
        super(theCode, message, cause);
        this.mStatusCode = theCode;
    }

    /**
     * Special constructor when the message does not exists.
     * This substitutes the constructor ParseException(Throwable cause) which
     * does not need the code. This is reliable because from ParseException,
     * all the status code is set. So, at least the parse exception is null
     * it is not possible that the code is empty.
     * @param theCode
     * @param cause
     */
    private ParseResponse(int theCode, Throwable cause) {
        super(cause);
        this.mStatusCode = theCode;
    }

    @Override
    public int getCode() {
        return mStatusCode;
    }

    public boolean isError() {
        return mStatusCode != REQUEST_CORRECT;
    }

    /**
     * Get a response message that is human redable
     * @param context
     *     The context provided to display the message
     * @return
     *     The human redable message depending on the status code
     */
    public String getHumanRedableResponseMessage(Context context) {
        // Initialize the exception message with the generic error message
        String resultMessage = context.getResources().getString(R.string.correct_request_ok);

        switch (getCode()) {
            case REQUEST_CORRECT:
                resultMessage = context.getResources().getString(R.string.correct_request_ok);
                break;
            case ParseException.OTHER_CAUSE:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INTERNAL_SERVER_ERROR:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.CONNECTION_FAILED:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.OBJECT_NOT_FOUND:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_QUERY:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_CLASS_NAME:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.MISSING_OBJECT_ID:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_KEY_NAME:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_POINTER:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_JSON:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.COMMAND_UNAVAILABLE:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.NOT_INITIALIZED:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INCORRECT_TYPE:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_CHANNEL_NAME:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.PUSH_MISCONFIGURED:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.OBJECT_TOO_LARGE:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.OPERATION_FORBIDDEN:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.CACHE_MISS:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_NESTED_KEY:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_FILE_NAME:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_ACL:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.TIMEOUT:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_EMAIL_ADDRESS:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.DUPLICATE_VALUE:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_ROLE_NAME:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.EXCEEDED_QUOTA:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.SCRIPT_ERROR:
                resultMessage = context.getResources().getString(R.string.error_message_script_error);
                break;
            case ParseException.VALIDATION_ERROR:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_validation_failed);
                break;
            case ParseException.FILE_DELETE_ERROR:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.REQUEST_LIMIT_EXCEEDED:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_EVENT_NAME:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.USERNAME_MISSING:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.PASSWORD_MISSING:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.USERNAME_TAKEN:
                resultMessage = context.getString(R.string.error_message_username_already_taken);
                break;
            case ParseException.EMAIL_TAKEN:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.EMAIL_MISSING:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.EMAIL_NOT_FOUND:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.SESSION_MISSING:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.MUST_CREATE_USER_THROUGH_SIGNUP:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.ACCOUNT_ALREADY_LINKED:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_SESSION_TOKEN:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.LINKED_ID_MISSING:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.INVALID_LINKED_SESSION:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ParseException.UNSUPPORTED_SERVICE:
                // TODO: Implement this case
                resultMessage = context.getResources().getString(R.string.error_message_generic);
                break;
            case ERROR_VIDEO_NOT_FOUND:
                resultMessage = context.getResources().getString(R.string.error_message_video_not_found);
                break;
            case ERROR_LOGIN_WITH_FACEBOOK:
                resultMessage = context.getResources().getString(R.string.error_message_login_with_facebook);
                break;
            case ERROR_LOGIN_WITH_GOOGLE:
                resultMessage = context.getResources().getString(R.string.error_message_login_with_google);
                break;
            case ERROR_LOGIN_WITH_PARSE:
                resultMessage = context.getResources().getString(R.string.error_message_login_with_parse);
                break;
            case ERROR_USER_NOT_LOGGED_IN:
                resultMessage = context.getResources().getString(R.string.error_message_user_not_logged_in);
                break;
            default:
                Log.w(TAG, "Request status not recognized " + getCode());
                break;
        }
        return resultMessage;
    }

    public static class Builder {
        // The status code is reset to minimum value
        private int mStatusCode = Integer.MIN_VALUE;
        private String mMessage;
        private Throwable mCause;

        public Builder (ParseException parseException) {
            if (parseException == null) {
                return;
            }

            this.mStatusCode = parseException.getCode();
            this.mMessage = parseException.getMessage();
            this.mCause = parseException.getCause();
        }

        public Builder statusCode(int statusCode) {
            this.mStatusCode = statusCode;
            return this;
        }

        public ParseResponse build() {
            // If the status code is not set, which is only possible when the
            // ParseException is null, then return correct value
            if (mStatusCode == Integer.MIN_VALUE) {
                return new ParseResponse();
            }

            // mMessage is null
            if (mMessage == null) {
                // mMessage is null and mCause is null
                if (mCause == null) {
                    return new ParseResponse(mStatusCode);
                // mMessage is null and mCause is not null
                } else {
                    return new ParseResponse(mStatusCode, mCause);
                }
            } else {
                // mMessage is not null and mCause is null
                if (mCause == null) {
                    return new ParseResponse(mStatusCode, mMessage);
                // mMessage is not null and mCause is not null
                } else {
                    return new ParseResponse(mStatusCode, mMessage, mCause);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ParseResponse{" +
                "mStatusCode=" + mStatusCode +
                '}';
    }
}
