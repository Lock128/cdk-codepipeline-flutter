export const handler = async (event) => {
  try {

  const Country = event?.queryStringParameters?.Country;

  const res = {
    statusCode: 200,
    body: "-1"
    ,
  };

  if (Country=="Germany") {
    res.body = "18";
  }
  else if (Country=="USA") {
    res.body = "21";
  }
  else if (Country=="Sweden") {
    res.body = "18";
  } else {
    console.log(`Not found country: ${Country}`);
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