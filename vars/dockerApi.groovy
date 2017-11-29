#!/usr/bin/env groovy
import com.pipeline.libs.DockerRegistry

def call(def img, int n){

    echo "Running Docker Registry cleaning procedure"
    def r = new DockerRegistry(this)
    r.connect(img)
    r.purge(n)

}

