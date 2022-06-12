
export const handler = async (event) => {
  try {
    const Country = event?.queryStringParameters?.Country;
    const res = {
      statusCode: 200,
      body: `Not found country: ${Country}`
    };

    if (!verifyAllowedCountry(Country)) {
      console.log(`Allowed country not found: ${Country}`);
      res.body = "Unknown";
    }

    if (Country == "Germany") {
      res.body = "A4";
    }
    else if (Country == "USA") {
      res.body = "Letter";
    }
    else if (Country == "Argentina") {
      res.body = "A4";
    } else {
      console.log(`Not found country: ${Country}`);
      console.log(`Event: ${event}`);
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

let verifyAllowedCountry = function (Country: string): boolean {
  let listAllowedCountries: Array<string> = ["Germany", "India", "USA", "Mexico", "Argentina"];
  return listAllowedCountries.includes(Country);
};
