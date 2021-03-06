package com.badoo.chateau.example.data.repos.messages;

import android.support.annotation.NonNull;

import com.badoo.chateau.core.repos.messages.MessageQueries;
import com.badoo.chateau.data.models.payloads.TextPayload;
import com.badoo.chateau.example.data.model.ExampleMessage;
import com.badoo.chateau.example.data.repos.messages.ParseMessageDataSource.ImageUploader;
import com.badoo.chateau.example.data.util.ParseHelper;
import com.badoo.chateau.example.data.util.ParseUtils.MessagesTable;
import com.badoo.unittest.rx.BaseRxTestCase;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.hamcrest.CustomMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ParseMessageDataSourceTest extends BaseRxTestCase {

    private static final String TEST_CHAT_ID = "chatId";

    @Mock
    private ImageUploader mImageUploader;
    @Mock
    private ParseHelper mMockParseHelper;

    private ParseMessageDataSource mTarget;

    // Use to listen for publishes of sent messages
    private Observable<List<ExampleMessage>> mMessages;

    @Before
    public void beforeTest() {
        super.beforeTest();
        mTarget = new ParseMessageDataSource(mImageUploader, mMockParseHelper);
        mMessages = mTarget.subscribeToMessages(new MessageQueries.SubscribeToMessagesQuery<>(TEST_CHAT_ID));
        mockCurrentUser("userId");
    }

    @Test
    public void sendTextMessage() {
        // Setup
        final TextPayload textPayload = new TextPayload("Hello world");
        final MessageQueries.SendMessageQuery message = new MessageQueries.SendMessageQuery(TEST_CHAT_ID, textPayload.getMessage(), null);
        final TestSubscriber<List<ExampleMessage>> testSubscriber = new TestSubscriber<>();

        // Execute
        mMessages.subscribe(testSubscriber);
        mTarget.sendMessage(message);

        // Assert
        verify(mMockParseHelper).saveInBackground(argThat(new CustomMatcher<ParseObject>("") {
            @Override
            public boolean matches(Object item) {
                return ((ParseObject) item).getClassName().equals(MessagesTable.NAME);
            }
        }), any());
        testSubscriber.assertValueCount(1);
    }

    private ParseUser mockCurrentUser(@NonNull String userId) {
        final ParseUser mockUser = mock(ParseUser.class);
        when(mockUser.getObjectId()).thenReturn(userId);
        when(mMockParseHelper.getCurrentUser()).thenReturn(mockUser);
        return mockUser;
    }

}
