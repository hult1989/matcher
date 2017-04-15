from fabric.api import *

env.hosts = ['ubuntu@configserver']
env.key_filename = '~/hermes'

def sync():
    put('./target/event-generator-fat.jar', '~/storm/event-generator-fat.jar')
    put('./target/sub-generator-fat.jar', '~/storm/sub-generator-fat.jar')
    put('./generator.cfg', '~/storm/generator.cfg')

