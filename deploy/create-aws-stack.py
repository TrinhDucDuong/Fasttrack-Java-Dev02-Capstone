import argparse
import base64
from pathlib import Path
import secrets

import boto3
from botocore.exceptions import ClientError

parser = argparse.ArgumentParser()
parser.add_argument('--connection-arn', required=True)
parser.add_argument('--region', default='ap-southeast-2')
args = parser.parse_args()
session = boto3.Session(region_name=args.region)
ssm = session.client('ssm')
cfn = session.client('cloudformation')
connections = session.client('codeconnections')
connection = connections.get_connection(ConnectionArn=args.connection_arn)['Connection']
if connection['ConnectionStatus'] != 'AVAILABLE':
    raise SystemExit('GitHub connection must be AVAILABLE before creating infrastructure')

def secure_parameter(name, factory):
    path = '/studycoach/prod/' + name
    try:
        return ssm.get_parameter(Name=path, WithDecryption=True)['Parameter']['Value']
    except ClientError as error:
        if error.response['Error']['Code'] != 'ParameterNotFound':
            raise
    value = factory()
    ssm.put_parameter(Name=path, Value=value, Type='SecureString', Tags=[{'Key':'Project','Value':'study-coach'}])
    return value

db_password = secure_parameter('db-password', lambda: secrets.token_hex(24))
secure_parameter('jwt-secret', lambda: base64.b64encode(secrets.token_bytes(48)).decode())
secure_parameter('rabbitmq-password', lambda: secrets.token_hex(24))
for name, value in [('db-username', 'study_coach'), ('rabbitmq-username', 'study_coach')]:
    ssm.put_parameter(Name='/studycoach/prod/' + name, Value=value, Type='String', Overwrite=True)

prefixes = session.client('ec2').describe_managed_prefix_lists(Filters=[{'Name':'prefix-list-name','Values':['com.amazonaws.global.cloudfront.origin-facing']}])['PrefixLists']
versions = session.client('rds').describe_db_engine_versions(Engine='postgres', EngineVersion='16')['DBEngineVersions']
version = max((v['EngineVersion'] for v in versions), key=lambda v: tuple(map(int, v.split('.'))))
parameters = {'CloudFrontPrefixListId': prefixes[0]['PrefixListId'], 'PostgresEngineVersion': version, 'DbPassword': db_password, 'GitHubConnectionArn': args.connection_arn}
response = cfn.create_stack(StackName='study-coach', TemplateBody=Path(__file__).with_name('aws-infrastructure.yaml').read_text(), Parameters=[{'ParameterKey':key,'ParameterValue':value} for key,value in parameters.items()], Capabilities=['CAPABILITY_NAMED_IAM'], Tags=[{'Key':'Project','Value':'study-coach'}], OnFailure='ROLLBACK')
print('Stack submitted:', response['StackId'])
