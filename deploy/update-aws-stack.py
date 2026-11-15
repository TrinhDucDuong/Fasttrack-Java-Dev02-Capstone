import argparse
from pathlib import Path

import boto3
from botocore.exceptions import ClientError

parser = argparse.ArgumentParser()
parser.add_argument('--region', default='ap-southeast-2')
parser.add_argument('--stack-name', default='study-coach')
args = parser.parse_args()

cloudformation = boto3.client('cloudformation', region_name=args.region)
stack = cloudformation.describe_stacks(StackName=args.stack_name)['Stacks'][0]
parameters = [
    {'ParameterKey': parameter['ParameterKey'], 'UsePreviousValue': True}
    for parameter in stack.get('Parameters', [])
]

try:
    response = cloudformation.update_stack(
        StackName=args.stack_name,
        TemplateBody=Path(__file__).with_name('aws-infrastructure.yaml').read_text(),
        Parameters=parameters,
        Capabilities=['CAPABILITY_NAMED_IAM'],
    )
except ClientError as error:
    if 'No updates are to be performed' in str(error):
        print('Stack is already up to date')
        raise SystemExit(0)
    raise

print('Stack update submitted:', response['StackId'])
cloudformation.get_waiter('stack_update_complete').wait(StackName=args.stack_name)
print('Stack update completed')
