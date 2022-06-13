import { SSM } from "aws-sdk";

export const handler = async (event) => {
  try {
    const res = {
      statusCode: 200,
      body: `Finished processing but no build was started.`
    };

//start build calling POST request
//https://api.codemagic.io/builds
//post details - {"appId": "62a278dc0a17acfc470d062d","workflowId": "62a278dc0a17acfc470d062c","branch": "main"}
//should be parameters (!)

// needs header x-auth-token
await getParameterWorker("/tool/CodeMagic", true).then((value) => {
      console.log("Found password for CodeMagic from SystemManager");
    });



    return res;
  } catch (err) {
    console.log(err);
    const res = {
      statusCode: 400,
      body: JSON.stringify("Something went wrong!"),
    };
    return res;
  }
};

const getParameterWorker = async (name:string, decrypt:boolean) : Promise<string> => {
    const ssm = new SSM();
    const result = await ssm
    .getParameter({ Name: name, WithDecryption: decrypt })
    .promise();
    return result.Parameter.Value;
}

export const getParameter = async (name:string) : Promise<string> => {
    return getParameterWorker(name,false);
}

export const getEncryptedParameter = async (name:string) : Promise<string> => {
    return getParameterWorker(name,true);
}

