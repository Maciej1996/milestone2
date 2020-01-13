package com.example.customkeyboard;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class KeyboardServiceIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private Keyboard mKeyBoard;
    private KeyboardView mKeyView;


    @Override
    public View onCreateInputView() {
        mKeyView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        mKeyBoard = new Keyboard(this, R.xml.qwerty);
        mKeyView.setKeyboard(mKeyBoard);
        mKeyView.setOnKeyboardActionListener(this);
        return mKeyView;
    }


    // Listeners
    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {
        // no-op
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);


        //1
        if (primaryCode == 1) {
            String mess = "Hello I’m custom bluetooth keyboard!!!";
            InputConnection inputConnection = getCurrentInputConnection();
            inputConnection.commitText(String.valueOf(mess), 1);
            am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
        }
        //2
        if (primaryCode == 2) {
            String mess = "616000010000123";
            InputConnection inputConnection = getCurrentInputConnection();
            inputConnection.commitText(String.valueOf(mess), 1);
            am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
        }
        //3
        if (primaryCode == 3) {
            String mess = "616000010000124";
            InputConnection inputConnection = getCurrentInputConnection();
            inputConnection.commitText(String.valueOf(mess), 1);
            am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);

        }
        if (primaryCode == 4) {
            InputMethodManager imeManager = (InputMethodManager)
                    getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
            imeManager.showInputMethodPicker();
        }
        if (primaryCode == 5) {

            InputConnection inputConnection = getCurrentInputConnection();
            inputConnection.deleteSurroundingText(50,50);
            am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
        }


    }



    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {
        // no-op
    }

    @Override
    public void swipeRight() {
        // no-op
    }

    @Override
    public void swipeDown() {
        // no-op
    }

    @Override
    public void swipeUp() {
        // no-op
    }
}