#!/usr/bin/env groovy
import DockerRegistry

def call(body){

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node{
        echo "hello from library"
    }
}

