interface InputEvent {
  Country: string;
}

export const handler = async (event: InputEvent): Promise<string> => {
  const { Country } = event;
  if (!verifyAllowedCountry(Country)) {
    console.log(`Not found country: ${Country}`);
    return "Unknown";
  }
  if (Country=="Germany") {
    return "A4";
  }
  else if (Country=="USA") {
    return "Letter";
  }
  else if (Country=="Argentina") {
    return "A4";
  }
  console.log(`Not found country: ${Country}`);
  return "Unknown";
};


let calculateMinAge = async function (Country: string): Promise<number> {
  if (Country=="Germany") {
    return 18;
  }
  else if (Country=="USA") {
    return 21;
  }
  console.log(`Not found country: ${Country}`);
  return -1;
};

let verifyAllowedCountry = function (Country: string): boolean {
  let listAllowedCountries: Array<string> = ["Germany", "India", "USA", "Mexico", "Argentina"];
  return listAllowedCountries.includes(Country);
};
