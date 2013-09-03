A simple web service for editing pdfs, which allows to add text and images. It uses itext library to modify pdf.

## API
### POST /pdfeditor/
Add text/image blocks to pdf.

Request example:

    {
      "referenceWidth":1000,
      "pdfUrl":"http://somehost/pdfs/1520045.pdf",
      "blocks":[
        {
          "font": "Arial",
          "fontSize":22,
          "color":"FF0000",
          "type":"text",
          "x":122,
          "y":485,
          "page":1,
          "text":"Hello"
        },
        {
          "width":200,
          "height":100,
          "type":"image",
          "x":582,
          "y":443,
          "page":1,
          "url":"http://somehost/images/8d076e76-cfd4-44c5-bce3-0cf6b0adc9e4.png"
        }
      ]
    }

Response example:

    {
      "pdf_url":"/pdfeditor/generated_pdfs/65858f01e7ccd8fd337df0482a422ac6124fcfe3.pdf"
    }

### GET /pdfeditor/generated_pdfs/:filename
Get modified pdf by url provided in response.

## Deployment
This is a play 2.0.5 application. After installing scala and play, service can be started by

    play start