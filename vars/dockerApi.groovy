#!/usr/bin/env groovy
import com.pipeline.libs.DockerRegistry

def call(){
/*
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
*/
    echo "Hello from my lib"
    def r = new DockerRegistry()

}

