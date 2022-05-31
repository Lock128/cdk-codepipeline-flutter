interface InputEvent {
  Country: string;
}

export const handler = async (event: InputEvent): Promise<number> => {
  const { Country } = event;
  if (Country=="Germany") {
    return 18;
  }
  else if (Country=="USA") {
    return 21;
  }
  else if (Country=="Sweden") {
    return 18;
  }
  console.log(`Not found country: ${Country}`);
  return -1;
};


let calculateMinAge = async function (Country: string): Promise<number> {
  if (Country=="Germany") {
    return 18;
  }
  else if (Country=="USA") {
    return 21;
  }
  else if (Country=="India") {
    return 18;
  }
  else if (Country=="Argentina") {
    return 18;
  }
  console.log(`Not found country: ${Country}`);
  return -1;
};

let verifyAllowedCountry = function (Country: string): boolean {
  let listAllowedCountries: Array<string> = ["Germany", "India", "USA", "Mexico", "Argentina"];
  return listAllowedCountries.includes(Country);
};
