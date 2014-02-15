package com.couchbase.lite.testapp.ektorp.tests;

import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.ektorp.CBLiteHttpClient;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.support.HttpClientFactory;

import junit.framework.Assert;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.ReplicationStatus;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Replicator extends CBLiteEktorpTestCase {

    public void testPush() throws IOException {

        CountDownLatch doneSignal = new CountDownLatch(1);
        HttpClient httpClient = new CBLiteHttpClient(manager);
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

        // create a local database
        CouchDbConnector dbConnector = dbInstance.createConnector(DEFAULT_TEST_DB, true);

        // create 3 objects
        TestObject test1 = new TestObject(1, false, "ektorp-1");
        TestObject test2 = new TestObject(2, false, "ektorp-2");
        TestObject test3 = new TestObject(3, false, "ektorp-3");

        // save these objects in the database
        dbConnector.create(test1);
        dbConnector.create(test2);
        dbConnector.create(test3);

        // push this database to the test replication server
        ReplicationCommand pushCommand = new ReplicationCommand.Builder()
            .source(DEFAULT_TEST_DB)
            .target(getReplicationURL().toExternalForm())
            .continuous(false)
            .createTarget(true)
            .build();

        ReplicationStatus status = dbInstance.replicate(pushCommand);
        Replication repl = database.getReplicator(status.getSessionId());
        ReplicationChangeListener replicationObserver = new ReplicationChangeListener(doneSignal);
        repl.addChangeListener(replicationObserver);

        try {
            boolean success = doneSignal.await(30, TimeUnit.SECONDS);
            assertTrue(success);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(status.getSessionId());

    }

    public void testFilteredPush() throws IOException {

        CountDownLatch doneSignal = new CountDownLatch(1);

        // install the filter
        database.setFilter("evenFoo", new ReplicationFilter() {

            @Override
            public boolean filter(SavedRevision revision, Map<String, Object> params) {
                Integer foo = (Integer) revision.getProperties().get("foo");
                if (foo != null && foo.intValue() % 2 == 0) {
                    return true;
                }
                return false;
            }
        });

        HttpClient httpClient = new CBLiteHttpClient(manager);
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

        // create a local database
        CouchDbConnector dbConnector = dbInstance.createConnector(DEFAULT_TEST_DB, true);

        // create 3 objects
        TestObject test1 = new TestObject(1, false, "ektorp-1");
        TestObject test2 = new TestObject(2, false, "ektorp-2");
        TestObject test3 = new TestObject(3, false, "ektorp-3");

        // save these objects in the database
        dbConnector.create(test1);
        dbConnector.create(test2);
        dbConnector.create(test3);

        // push this database to the test replication server
        ReplicationCommand pushCommand = new ReplicationCommand.Builder()
            .source(DEFAULT_TEST_DB)
            .target(getReplicationURL().toExternalForm())
            .continuous(false)
            .filter("evenFoo")
            .build();

        ReplicationStatus status = dbInstance.replicate(pushCommand);
        Replication repl = database.getReplicator(status.getSessionId());

        ReplicationChangeListener replicationObserver = new ReplicationChangeListener(doneSignal);
        repl.addChangeListener(replicationObserver);

        try {
            boolean success = doneSignal.await(30, TimeUnit.SECONDS);
            assertTrue(success);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(status.getSessionId());

    }

    public void testPull() throws IOException {

        CountDownLatch doneSignal = new CountDownLatch(1);

        HttpClient httpClient = new CBLiteHttpClient(manager);
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

        // create a local database
        CouchDbConnector dbConnector = dbInstance.createConnector(DEFAULT_TEST_DB, true);

        // push this database to the test replication server
        ReplicationCommand pushCommand = new ReplicationCommand.Builder()
            .source(getReplicationURL().toExternalForm())
            .target(DEFAULT_TEST_DB)
            .continuous(false)
            .build();

        ReplicationStatus status = dbInstance.replicate(pushCommand);
        Replication repl = database.getReplicator(status.getSessionId());

        ReplicationChangeListener replicationObserver = new ReplicationChangeListener(doneSignal);
        repl.addChangeListener(replicationObserver);

        try {
            boolean success = doneSignal.await(30, TimeUnit.SECONDS);
            assertTrue(success);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(status.getSessionId());

    }


    public void testPullWithObserver() throws IOException {

        CountDownLatch doneSignal = new CountDownLatch(1);
        HttpClient httpClient = new CBLiteHttpClient(manager);
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

        // push this database to the test replication server
        ReplicationCommand pushCommand = new ReplicationCommand.Builder()
            .source(getReplicationURL().toExternalForm())
            .target(DEFAULT_TEST_DB)
            .continuous(false)
            .build();

        ReplicationStatus status = dbInstance.replicate(pushCommand);
        Replication repl = database.getReplicator(status.getSessionId());
        ReplicationChangeListener replicationObserver = new ReplicationChangeListener(doneSignal);
        repl.addChangeListener(replicationObserver);


        Assert.assertNotNull(status.getSessionId());
        Assert.assertEquals(repl.getSessionID(), status.getSessionId());

        try {
            boolean success = doneSignal.await(30, TimeUnit.SECONDS);
            assertTrue(success);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(replicationObserver.isReplicationFinished());

        	

    }

    public void testPullWithoutCredentialsInURL() throws IOException {

        CountDownLatch doneSignal = new CountDownLatch(1);

        manager.setDefaultHttpClientFactory(new HttpClientFactory() {

            @Override
            public org.apache.http.client.HttpClient getHttpClient() {
                DefaultHttpClient httpClient = new DefaultHttpClient();

                BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();

                //store whatever credentials we have in the credentials provider
                AuthScope authScope = new AuthScope(getReplicationServer(), getReplicationPort());
                Credentials authCredentials = new UsernamePasswordCredentials(getReplicationAdminUser(), getReplicationAdminPassword());
                credsProvider.setCredentials(authScope, authCredentials);

                httpClient.setCredentialsProvider(credsProvider);

                //set credentials pre-emptively
                HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {

                    @Override
                    public void process(HttpRequest request,
                            HttpContext context) throws HttpException,
                            IOException {
                        AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
                        CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                                ClientContext.CREDS_PROVIDER);
                        HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

                        if (authState.getAuthScheme() == null) {
                            AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                            Credentials creds = credsProvider.getCredentials(authScope);
                            authState.setCredentials(creds);
                            authState.setAuthScheme(new BasicScheme());
                        }
                    }
                };

                httpClient.addRequestInterceptor(preemptiveAuth, 0);

                return httpClient;
            }
        });

        HttpClient httpClient = new CBLiteHttpClient(manager);
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

        // create a local database
        CouchDbConnector dbConnector = dbInstance.createConnector(DEFAULT_TEST_DB, true);

        // push this database to the test replication server
        ReplicationCommand pushCommand = new ReplicationCommand.Builder()
            .source(getReplicationURLWithoutCredentials().toExternalForm())
            .target(DEFAULT_TEST_DB)
            .continuous(false)
            .build();

        ReplicationStatus status = dbInstance.replicate(pushCommand);
        Replication repl = database.getReplicator(status.getSessionId());
        ReplicationChangeListener replicationObserver = new ReplicationChangeListener(doneSignal);
        repl.addChangeListener(replicationObserver);

        try {
            boolean success = doneSignal.await(30, TimeUnit.SECONDS);
            assertTrue(success);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(status.getSessionId());


    }

    // this test is short-circuited because the underlying feature (replication localdb <-> localdb) doesn't work yet
    public void disabledTestPushToLocal() throws Exception, IOException {

        Database other = manager.getExistingDatabase(DEFAULT_TEST_DB + "2");
        if(other != null) {
            other.delete();
        }

        HttpClient httpClient = new CBLiteHttpClient(manager);
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

        // create a local database
        CouchDbConnector dbConnector = dbInstance.createConnector(DEFAULT_TEST_DB, true);

        // create 3 objects
        TestObject test1 = new TestObject(1, false, "ektorp-1");
        TestObject test2 = new TestObject(2, false, "ektorp-2");
        TestObject test3 = new TestObject(3, false, "ektorp-3");

        // save these objects in the database
        dbConnector.create(test1);
        dbConnector.create(test2);
        dbConnector.create(test3);

        // push this database to the test replication server
        ReplicationCommand pushCommand = new ReplicationCommand.Builder()
            .source(DEFAULT_TEST_DB)
            .target(DEFAULT_TEST_DB + "2")
            .continuous(false)
            .createTarget(true)
            .build();

        ReplicationStatus status = dbInstance.replicate(pushCommand);
        try {
            Thread.sleep(60*1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertNotNull(status.getSessionId());

    }

    class ReplicationChangeListener implements Replication.ChangeListener {

        public boolean replicationFinished = false;
        private CountDownLatch doneSignal;

        ReplicationChangeListener(CountDownLatch doneSignal) {
            super();
            this.doneSignal = doneSignal;
        }

        @Override
        public void changed(Replication.ChangeEvent event) {
            Log.d(TAG, "ReplicationObserver.update called.  observable: " + event.getSource());
            Replication replicator = (Replication) event.getSource();
            if (!replicator.isRunning()) {
                replicationFinished = true;
                String msg = String.format("myobserver.update called, set replicationFinished to: %b", replicationFinished);
                Log.d(TAG, msg);
                doneSignal.countDown();
            }
            else {
                String msg = String.format("myobserver.update called, but replicator still running, so ignore it");
                Log.d(TAG, msg);
            }
        }

        public boolean isReplicationFinished() {
            return replicationFinished;
        }
    }


}

