import React, { useEffect, useRef, useState} from "react";

const Address = ({onAddressSelected, value}) => {
    const wrapRef = useRef(null);
    const [postcodeData, setPostcodeData] = useState({
        zonecode: "",
        address: "",
        detailAddress: "",
        extraAddress: "",
    });

    useEffect(() => {
        const script = document.createElement("script");
        script.src = "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
        script.async = true;
        document.head.appendChild(script);
    }, []);

    const execDaumPostcode = () => {
        const currentScroll = Math.max(document.body.scrollTop, document.documentElement.scrollTop);

        new window.daum.Postcode({
            oncomplete: function (data) {
                let addr = "";
                let extraAddr = "";

                if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
                    addr = data.roadAddress;
                } else { // 사용자가 지번 주소를 선택했을 경우(J)
                    addr = data.jibunAddress;
                }

                // 사용자가 선택한 주소가 도로명 타입일때 참고항목을 조합한다.
                if (data.userSelectedType === 'R') {
                    // 법정동명이 있을 경우 추가한다. (법정리는 제외)
                    // 법정동의 경우 마지막 문자가 "동/로/가"로 끝난다.
                    if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
                        extraAddr += data.bname;
                    }
                    // 건물명이 있고, 공동주택일 경우 추가한다.
                    if (data.buildingName !== '' && data.apartment === 'Y') {
                        extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                    }
                    // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
                    if (extraAddr !== '') {
                        extraAddr = ' (' + extraAddr + ')';
                    }
                }

                setPostcodeData({
                    ...postcodeData,
                    zonecode: data.zonecode,
                    address: addr,
                    extraAddress: extraAddr,
                });

                if (onAddressSelected) {
                    const fullAddress = `${data.zonecode} ${addr}${extraAddr ? " " + extraAddr : ""} ${postcodeData.detailAddress}`;
                    onAddressSelected(fullAddress);
                }

                wrapRef.current.style.display = 'none';
                document.body.scrollTop = currentScroll;
            },
            onresize: function (size) {
                wrapRef.current.style.height = size.height + 'px';
            },
            width: '100%',
            height: '100%'
        }).embed(wrapRef.current);

        wrapRef.current.style.display = 'block';
    };

    return (
        <>
            <input type="text" value={postcodeData.zonecode} placeholder="우편번호" readOnly/>
            <input type="button" onClick={execDaumPostcode} value="우편번호 찾기"/><br/>
            <input type="text" value={value || postcodeData.address} placeholder="주소" readOnly/><br/>
            <input
                type="text"
                value={postcodeData.detailAddress}
                placeholder="상세주소"
                onChange={(e) => {
                    const updated = {...postcodeData, detailAddress: e.target.value};
                    setPostcodeData(updated);
                    if (onAddressSelected) {
                        const fullAddress = `${updated.zonecode} ${updated.address}${updated.extraAddress ? " " + updated.extraAddress : ""} ${updated.detailAddress}`;
                        onAddressSelected(fullAddress);
                    }
                }}
            />
            <input type="text" value={postcodeData.extraAddress} placeholder="참고항목" readOnly/>

            <div ref={wrapRef}
                 style={{
                     display: "none",
                     border: "1px solid",
                     width: "100%",
                     height: "300px",
                     position: "relative",
                     bottom: "350px"
                 }}
            >
                <img
                    src="//t1.daumcdn.net/postcode/resource/images/close.png"
                    style={{
                        cursor: "pointer",
                        position: "absolute",
                        right: "0px",
                        top: "-1px",
                        zIndex: 1
                    }}
                    onClick= {() => (wrapRef.current.style.display = 'none')}
                    alt="접기 버튼"/>
            </div>
        </>
    )
}

export default Address;