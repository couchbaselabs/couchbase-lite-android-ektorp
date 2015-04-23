package com.couchbase.lite.ektorp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import org.ektorp.http.HttpResponse;
import org.ektorp.util.Exceptions;

import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.router.Router;
import com.couchbase.lite.router.RouterCallbackBlock;
import com.couchbase.lite.router.URLConnection;

public class CBLiteHttpResponse implements HttpResponse, RouterCallbackBlock {

    private URLConnection conn;
    private Router router;
    private InputStream is;
    final CountDownLatch doneSignal = new CountDownLatch(1);

    public static CBLiteHttpResponse of(URLConnection conn, Router router) throws IOException {
        return new CBLiteHttpResponse(conn, router);
    }

    private CBLiteHttpResponse(URLConnection conn, Router router) throws IOException {
        this.conn = conn;
        this.router = router;
    }

    @Override
    public void abort() {
        router.stop();
    }

    @Override
    public int getCode() {
        getContent();
        return conn.getResponseCode();
    }

    @Override
    public InputStream getContent() {
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            Log.e(Database.TAG, "Interupted waiting for response", e);
        }
        return is;
    }

    @Override
    public String getETag() {
        return conn.getHeaderField("ETag");
    }

    @Override
    public long getContentLength() {
        getContent();
        return conn.getContentLength();
    }

    @Override
    public String getContentType() {
        getContent();
        return conn.getContentType();
    }

    @Override
    public String getRequestURI() {
        try {
            return conn.getURL().toURI().toString();
        } catch (URISyntaxException e) {
            throw Exceptions.propagate(e);
        }
    }

    @Override
    public boolean isSuccessful() {
        return conn.getResponseCode() < 300;
    }

    @Override
    public void releaseConnection() {
        try {
            router.stop();  // needed to clean up changes listeners
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /** RouterCallbackBlock **/

    @Override
    public void onResponseReady() {
        doneSignal.countDown();
        is = conn.getResponseInputStream();
    }

}
