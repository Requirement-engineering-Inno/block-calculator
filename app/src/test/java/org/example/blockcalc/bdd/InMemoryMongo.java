package org.example.blockcalc.bdd;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import java.net.InetSocketAddress;

/** A pure-Java in-memory MongoDB, started once per test JVM. No Docker and no binary download needed. */
final class InMemoryMongo {

    private static final MongoServer SERVER = new MongoServer(new MemoryBackend());
    private static final InetSocketAddress ADDRESS = SERVER.bind();

    private InMemoryMongo() {
    }

    static String uri() {
        return "mongodb://localhost:" + ADDRESS.getPort() + "/blockcalc";
    }
}
