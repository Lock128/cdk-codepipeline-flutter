import { SSM } from "aws-sdk";

type StartBuildResponse = {
	buildId: string;
};


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
		const buildIOSApp = await getParameterWorker("/codepipeline/build-ios-app", true).then((value) => {
			console.log("Found value for for /codepipeline/build-ios-app from SystemManager: " + value);
			return value;
		});
		console.log("Build iOS Status in SystemManager Parameter " + buildIOSApp);
		if (buildIOSApp !== "true") {
			console.log("Build iOS Dashboard is deactivated");
			return res;
		}
		// needs header x-auth-token
		const codeMagicToken = await getParameterWorker("/tool/CodeMagic", true).then((value) => {
			console.log("Found password for CodeMagic from SystemManager");
			return value;
		});

		const axios = require('axios').default;

		try {
			//️ const response: Response
			// {"appId": "62a278dc0a17acfc470d062d","workflowId": "62a278dc0a17acfc470d062c","branch": "main"}

			const data = JSON.stringify({
				appId: '62a278dc0a17acfc470d062d',
				workflowId: '62a278dc0a17acfc470d062c',
				branch: 'main'
			});

			const options = {
				headers: {
					'Content-Type': 'application/json',
					Accept: 'application/json',
					'x-auth-token': codeMagicToken
				},
			};
			const response = await axios.post("https://api.codemagic.io/builds", data, options);

			if (response.status != 200) {
				throw new Error(`Error! status: ${response.statusText}`);
			}

			// ️ const result: StartBuildResponse
			const result = (await response.data()) as StartBuildResponse;

			console.log('result is: ', JSON.stringify(result, null, 4));
		} catch (error) {
			if (error instanceof Error) {
				console.log('error message: ', error.message);
				return error.message;
			} else {
				console.log('unexpected error: ', error);
				return 'An unexpected error occurred';
			}
		}
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

const getParameterWorker = async (name: string, decrypt: boolean): Promise<string> => {
	const ssm = new SSM();
	const result = await ssm
		.getParameter({ Name: name, WithDecryption: decrypt })
		.promise();
	return result.Parameter.Value;
}

export const getParameter = async (name: string): Promise<string> => {
	return getParameterWorker(name, false);
}

export const getEncryptedParameter = async (name: string): Promise<string> => {
	return getParameterWorker(name, true);
}

